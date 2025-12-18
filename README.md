# 🧹 gbk_clearlag 1.1

**gbk_clearLag** is an advanced optimization plugin for Minecraft servers, designed to reduce lag, improve performance, and keep the server clean and stable, even with many players online.

_It performs automatic and manual cleanups of items, mobs, entities, and chunk limits, preventing server overload and TPS drops._

**⚙️ Main Features**
- ✔ Configurable automatic cleanup system
- ✔ Warning messages before cleanup
- ✔ Manual cleanup commands
- ✔ Mob, block, and portal limits per chunk
- ✔ Automatic blocking when limits are reached
- ✔ Protected items (never removed)
- ✔ Removal of unnamed mobs only
- ✔ Automatic cleanup in Nether and The End
- ✔ Fully multilingual message system (pt-BR, pt-PT, en-US, zh-CN)
- ✔ Customizable messages and prefix
✔ Update checker for server operators
✔ Compatible with Paper, Spigot, and Bukkit
✔ Developed with a strong focus on performance and stability

**🌍 Language System**
All plugin messages are dynamically loaded from messages.yml, allowing full translation and customization without modifying the code.

**🧹 Perfect for:**
- Survival servers
- SkyBlock servers
- Servers with heavy farms
- Small or large networks
- Servers suffering from lag or entity overload

🔽 [Download: **gbk_clearlag**](https://github.com/GabrielBk6/gbk_clearlag/releases/tag/clearlag) 

<img width="288" height="288" alt="img1" src="https://github.com/user-attachments/assets/557d179c-78c7-4fbe-ba25-ac9dc50ca36e" />

**1️⃣ Installation**
- ➡ Download the gbk_clearlag.jar file
- ➡ Place the file in the /plugins folder
➡ Start or restart the server
**2️⃣ Configuration**
- ➡ Open the config.yml file
*➡ Configure:*
- Automatic clearing interval
- Warning time before clearing
- Enable/disable clearing of items, mobs, and blocks
- Protected items (not removed)
- List of removable mobs
- Entity limits per chunk
- Discord webhook (optional)
- ➡ Save the file

**3️⃣ Commands**
- ➡ /gclear all → Performs a full clear
- ➡ /gclear mobs → clear all mobs
- ➡ /gclear items → clear all items
- ➡ /gclear reload → Reloads the plugin configuration
- (Commands are OP-only)

**4️⃣ Automatic Behavior**
- ➡ Sends chat warnings before clearing
- ➡ Automatically removes entities based on configuration
- ➡ Prevents excessive spawning and breeding per chunk
- ➡ Limits portals and prevents lag in the Nether and End

**5️⃣ Discord Logs (Optional)**
- ➡ Configure the webhook in config.yml
- ➡ Receive automatic clearing logs on Discord
