# SandPvP Essentials (Fabric, Minecraft Java 26.2)

Client-side PvP visuals. Nothing here changes gameplay or the server; it only changes how things are drawn.

Features: Small Pop (totem), Low/Off Fire, Small Handheld, Low Shield, per-particle filter (every registered
particle, with search / Disable All / Enable All / Reset), Custom Crosshair, Armor HUD, Potion HUD, FPS & Ping.
Everything is OFF by default, so the game looks vanilla until you turn something on.

Open the menu with the key **O** (change it in Options > Controls > Key Binds > "SandPvP Essentials").
Settings are saved in `.minecraft/config/sandpvp.json`.
Menu > Settings shows which render hooks are really installed (ACTIVE / NO MATCH / waiting).

---------------------------------------------------------------------------------------------

## How to turn this into a .jar (Android + GitHub Codespaces)

1. Download the ZIP: tap the download file card in the chat, it lands in your phone's Downloads folder.
   Name: `SandPvP-Essentials-Source.zip`

2. Make a new GitHub repository (so it does not mix with your old mod):
   github.com > "+" (top right) > New repository > Name: `sandpvp-essentials` > Public >
   tick "Add a README file" > Create repository.

3. Open Codespaces: on the repository page tap the green **Code** button > **Codespaces** tab >
   **Create codespace on main**. Wait until VS Code opens in the browser.

4. Upload the ZIP: in the Explorer (left, two-pages icon) long-press an empty area > **Upload...** >
   pick `SandPvP-Essentials-Source.zip`.

5. Open the terminal: the **Terminal** tab at the bottom (menu icon top left > Terminal > New Terminal
   if you do not see it). Run these one at a time:

       unzip -o SandPvP-Essentials-Source.zip && rm SandPvP-Essentials-Source.zip
       git add -A
       git commit -m "SandPvP Essentials"
       git push

6. Build: pushing starts the build on GitHub by itself (it installs Java 25 and Gradle 9.5.1 for you,
   the same way your first mod was built). Open github.com > your repo > **Actions** tab, wait for the
   green check. A red X means a compile error: open the run, copy the red error lines and send them to me.

7. Find the jar: repo page > **Releases** (right side) > **Latest build** > download
   `sandpvp-essentials-1.0.0.jar`. (The same release also has `api-dump.txt`, a list of Minecraft class
   signatures that helps me fix things if a hook shows NO MATCH.)

8. Install: copy the jar into the `mods` folder of your Fabric 26.2 profile in MojoLauncher/PojavLauncher,
   together with **Fabric API for 26.2** (download from Modrinth). Use Fabric Loader 0.19.3 or newer and Java 25.

9. Test: start the game, join a world, press **O** (or bind it in Controls). Check:
   - Controls > Key Binds shows the category "SandPvP Essentials".
   - Menu > Settings: look at the "Hook status" lines.
   - Turn on e.g. Custom Crosshair or FPS & Ping: you should see them immediately.
   - Particles > Disable All: particles stop appearing. Enable All brings them back.
   - Close and restart the game: your settings are still there.

Gradle wrapper note: the wrapper jar is a binary that has to be downloaded from the internet, which
was not possible while this project was written. The GitHub workflow installs Gradle itself, so you do
not need it. (If you ever want a local build: install Gradle 9.5.1 and Java 25, then run `gradle build`.)
