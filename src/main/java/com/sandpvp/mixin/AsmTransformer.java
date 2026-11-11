package com.sandpvp.mixin;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Small bytecode helper. It finds methods by NAME and by the TYPE of a parameter, never by a full
 * signature, so it keeps working when Minecraft changes the other parameters between versions.
 * Two kinds of edits:
 *  - wrap():  call a "begin" hook first and an "end" hook before every return (push/pop of a matrix)
 *  - guard(): call a hook first; if it returns true, return immediately (skips the method)
 */
final class AsmTransformer {
    private static final String HOOKS = "com/sandpvp/Hooks";

    static final String POSE = "com/mojang/blaze3d/vertex/PoseStack";
    static final String ITEM = "net/minecraft/world/item/ItemStack";
    static final String GFX = "net/minecraft/client/gui/GuiGraphicsExtractor";
    static final String PARTICLE_OPTIONS = "net/minecraft/core/particles/ParticleOptions";

    private AsmTransformer() {}

    static int wrap(ClassNode cn, Predicate<MethodNode> nameFilter, String requiredType, String optionalType,
                    String beginName, String endName) {
        int hooked = 0;
        for (MethodNode m : new ArrayList<>(cn.methods)) {
            if (!candidate(m) || !nameFilter.test(m) || alreadyHooked(m)) {
                continue;
            }
            if (Type.getReturnType(m.desc).getSort() != Type.VOID) {
                continue;
            }
            int required = slotOf(m, requiredType);
            if (required < 0) {
                continue;
            }
            int optional = optionalType == null ? -1 : slotOf(m, optionalType);

            String beginDesc = optionalType == null
                    ? "(L" + requiredType + ";)V"
                    : "(L" + requiredType + ";L" + optionalType + ";)V";
            String endDesc = "(L" + requiredType + ";)V";

            InsnList begin = new InsnList();
            begin.add(new VarInsnNode(Opcodes.ALOAD, required));
            if (optionalType != null) {
                if (optional >= 0) {
                    begin.add(new VarInsnNode(Opcodes.ALOAD, optional));
                } else {
                    begin.add(new InsnNode(Opcodes.ACONST_NULL));
                }
            }
            begin.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, beginName, beginDesc, false));

            for (AbstractInsnNode insn : m.instructions.toArray()) {
                if (insn.getOpcode() == Opcodes.RETURN) {
                    InsnList end = new InsnList();
                    end.add(new VarInsnNode(Opcodes.ALOAD, required));
                    end.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, endName, endDesc, false));
                    m.instructions.insertBefore(insn, end);
                }
            }
            m.instructions.insert(begin);
            m.maxStack = Math.max(m.maxStack, 3);
            hooked++;
        }
        return hooked;
    }

    /**
     * @param requiredType parameter type passed to the hook, or null for a hook without arguments
     * @param objectReturnOk also accept methods that return an object (then null is returned)
     */
    static int guard(ClassNode cn, Predicate<MethodNode> nameFilter, String requiredType, String hookName,
                     boolean objectReturnOk) {
        int hooked = 0;
        boolean expanded = expandedFrames(cn);
        for (MethodNode m : new ArrayList<>(cn.methods)) {
            if (!candidate(m) || !nameFilter.test(m) || alreadyHooked(m)) {
                continue;
            }
            int sort = Type.getReturnType(m.desc).getSort();
            boolean isVoid = sort == Type.VOID;
            boolean isObject = sort == Type.OBJECT || sort == Type.ARRAY;
            if (!isVoid && !(objectReturnOk && isObject)) {
                continue;
            }
            int slot = -1;
            if (requiredType != null) {
                slot = slotOf(m, requiredType);
                if (slot < 0) {
                    continue;
                }
            }
            String hookDesc = requiredType == null ? "()Z" : "(L" + requiredType + ";)Z";

            LabelNode continueLabel = new LabelNode();
            InsnList il = new InsnList();
            if (slot >= 0) {
                il.add(new VarInsnNode(Opcodes.ALOAD, slot));
            }
            il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, hookName, hookDesc, false));
            il.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
            if (isVoid) {
                il.add(new InsnNode(Opcodes.RETURN));
            } else {
                il.add(new InsnNode(Opcodes.ACONST_NULL));
                il.add(new InsnNode(Opcodes.ARETURN));
            }
            il.add(continueLabel);
            il.add(frameFor(cn, m, expanded));
            m.instructions.insert(il);
            m.maxStack = Math.max(m.maxStack, 2);
            hooked++;
        }
        return hooked;
    }

    private static boolean candidate(MethodNode m) {
        return (m.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0
                && !m.name.startsWith("<")
                && m.instructions != null
                && m.instructions.size() > 0;
    }

    private static boolean alreadyHooked(MethodNode m) {
        for (AbstractInsnNode insn : m.instructions.toArray()) {
            if (insn instanceof MethodInsnNode call && HOOKS.equals(call.owner)) {
                return true;
            }
        }
        return false;
    }

    /** Local variable slot of the first parameter with exactly this class type, or -1. */
    private static int slotOf(MethodNode m, String internalName) {
        int slot = (m.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
        for (Type type : Type.getArgumentTypes(m.desc)) {
            if (type.getSort() == Type.OBJECT && type.getInternalName().equals(internalName)) {
                return slot;
            }
            slot += type.getSize();
        }
        return -1;
    }

    /** Mixin hands us classes with expanded frames; check it instead of assuming. */
    private static boolean expandedFrames(ClassNode cn) {
        for (MethodNode m : cn.methods) {
            if (m.instructions == null) {
                continue;
            }
            for (AbstractInsnNode insn : m.instructions.toArray()) {
                if (insn instanceof FrameNode frame) {
                    return frame.type == Opcodes.F_NEW;
                }
            }
        }
        return true;
    }

    /** Stack map frame for the jump target at the very start of a method: locals are exactly the parameters. */
    private static FrameNode frameFor(ClassNode cn, MethodNode m, boolean expanded) {
        if (!expanded) {
            return new FrameNode(Opcodes.F_SAME, 0, null, 0, null);
        }
        List<Object> locals = new ArrayList<>();
        if ((m.access & Opcodes.ACC_STATIC) == 0) {
            locals.add(cn.name);
        }
        for (Type type : Type.getArgumentTypes(m.desc)) {
            switch (type.getSort()) {
                case Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.SHORT, Type.INT -> locals.add(Opcodes.INTEGER);
                case Type.FLOAT -> locals.add(Opcodes.FLOAT);
                case Type.LONG -> locals.add(Opcodes.LONG);
                case Type.DOUBLE -> locals.add(Opcodes.DOUBLE);
                case Type.ARRAY -> locals.add(type.getDescriptor());
                default -> locals.add(type.getInternalName());
            }
        }
        return new FrameNode(Opcodes.F_NEW, locals.size(), locals.toArray(), 0, new Object[0]);
    }
}
