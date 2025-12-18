# 🧹 gbk_clearlag
A Minecraft server optimization and lag-control plugin. Performs automatic and manual clearing of mobs and items, includes the /gclear command, limits entities per chunk, prevents excessive villager spawning and breeding, controls portals per chunk, and helps keep the server stable and organized.

🔨 Plugin System

➡ Automatic and manual entity clearing

➡ Control of entity overpopulation per chunk

➡ Prevention of excessive villagers

➡ Portal limitations

➡ Lag prevention in the Nether and End

⚙️ Settings

➡ Reads from config.yml:

➡ Automatic clearing interval

➡ Warning time before clearing

➡ Whether to clear items and/or mobs

➡ List of protected items (not removed)

➡ List of mobs that can be cleared
➡ Discord webhook (clearing logs)

🔽 [Download: **gbk_clearlag**](https://github.com/GabrielBk6/gbk_clearlag/releases/tag/clearlag) 

<img width="288" height="288" alt="img1" src="https://github.com/user-attachments/assets/557d179c-78c7-4fbe-ba25-ac9dc50ca36e" />

**1️⃣ Installation**
➡ Download the gbk_clearlag.jar file

➡ Place the file in the /plugins folder

➡ Start or restart the server

**2️⃣ Configuration**

➡ Open the config.yml file

➡ Configure:

Automatic clearing interval

Warning time before clearing

Enable/disable clearing of items, mobs, and blocks

Protected items (not removed)

List of removable mobs

Entity limits per chunk

Discord webhook (optional)

➡ Save the file

**3️⃣ Commands**
➡ /gclear all → Performs a full clear

➡ /gclear mobs → clear all mobs

➡ /gclear items → clear all items

➡ /gclear reload → Reloads the plugin configuration

(Commands are OP-only)

**4️⃣ Automatic Behavior**

➡ Sends chat warnings before clearing

➡ Automatically removes entities based on configuration

➡ Prevents excessive spawning and breeding per chunk

➡ Limits portals and prevents lag in the Nether and End

**5️⃣ Discord Logs (Optional)**

➡ Configure the webhook in config.yml

➡ Receive automatic clearing logs on Discord
