package me.gabrielbk;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class gbk_clearlag extends JavaPlugin implements TabCompleter, Listener {

    private int intervalo;
    private int aviso;
    private boolean limparItens;
    private boolean limparMobs;
    private boolean limparBlocos;
    private List<String> itensProtegidos;
    private List<String> mobsLimpos;
    private List<String> blocosLimpos;
    private String webhook;

    private FileConfiguration messages;

    private static final int LIMITE_VILLAGER_CHUNK = 30;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        carregarConfig();
        iniciarTarefaLixeira();
        getCommand("gclear").setTabCompleter(this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("✅ gbk_clearlag habilitado!");
    }

    @Override
    public void onDisable() {
        getLogger().info("❌ gbk_clearlag desabilitado!");
    }

    private void carregarConfig() {
        FileConfiguration config = getConfig();
        intervalo = config.getInt("intervalo-minutos", 40);
        aviso = config.getInt("aviso-cada-minutos", 10);
        limparItens = config.getBoolean("limpar.itens", true);
        limparMobs = config.getBoolean("limpar.mobs", true);
        limparBlocos = config.getBoolean("limpar.blocos", true);
        itensProtegidos = config.getStringList("itens-protegidos");
        mobsLimpos = config.getStringList("mobs-que-sao-limpos");
        blocosLimpos = config.getStringList("blocos-que-serao-limpos");
        webhook = config.getString("discord-webhook", "");
    }

    private void iniciarTarefaLixeira() {
        new BukkitRunnable() {
            int minutos = 0;

            @Override
            public void run() {
                minutos += aviso;
                if (minutos >= intervalo) {
                    broadcast(msg("iniciando_limpeza"));
                    int removidos = limparTudo();
                    broadcast(msg("limpeza_concluida").replace("{removed}", String.valueOf(removidos)));
                    sendDiscordLog("🧹 O lixeiro acabou de limpar o servidor! (" + removidos + " entidades removidas)");
                    minutos = 0;
                } else {
                    int restante = intervalo - minutos;
                    broadcast(msg("aviso_limpeza").replace("{minutes}", String.valueOf(restante)));
                }
                aplicarLimiteChunk();
            }
        }.runTaskTimer(this, 20L * 60L * aviso, 20L * 60L * aviso);
    }

    private int limparTudo() {
        int removidos = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {

                if (limparItens && e instanceof Item item) {
                    Material tipo = item.getItemStack().getType();
                    if (!itensProtegidos.contains(tipo.name())) {
                        item.remove();
                        removidos++;
                    }
                }

                if (limparMobs && e instanceof LivingEntity mob && !(e instanceof Player)) {
                    if (mob.getCustomName() != null) continue;
                    if (mobsLimpos.contains(mob.getType().name())) {
                        mob.remove();
                        removidos++;
                    }
                }

                if (limparBlocos && blocosLimpos.contains(e.getType().name())) {
                    e.remove();
                    removidos++;
                }
            }
        }
        return removidos;
    }

    private void aplicarLimiteChunk() {
        FileConfiguration config = getConfig();
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                Map<String, Integer> contagem = new HashMap<>();

                for (Entity e : chunk.getEntities()) {
                    String tipo = e.getType().name();
                    contagem.put(tipo, contagem.getOrDefault(tipo, 0) + 1);

                    int limiteMob = config.getInt("limites-chunk.mobs." + tipo, -1);
                    int limiteBloco = config.getInt("limites-chunk.blocos." + tipo, -1);

                    if ((limiteMob > 0 && contagem.get(tipo) > limiteMob)
                            || (limiteBloco > 0 && contagem.get(tipo) > limiteBloco)) {
                        e.remove();
                    }
                }

                int portalCount = 0;
                for (Block b : getChunkBlocks(chunk)) {
                    if (b.getType() == Material.NETHER_PORTAL || b.getType() == Material.END_PORTAL) {
                        portalCount++;
                        if (portalCount > 3) {
                            b.setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }

    // 🔒 BLOQUEIA SPAWN E REPRODUÇÃO DE VILLAGER ACIMA DE 30
    @EventHandler
    public void aoSpawnarVillager(CreatureSpawnEvent event) {
        if (event.getEntityType() != EntityType.VILLAGER) return;

        Chunk chunk = event.getLocation().getChunk();
        int villagers = contarEntidadesDoTipo(chunk, "VILLAGER");

        if (villagers >= LIMITE_VILLAGER_CHUNK) {
            event.setCancelled(true);
        }
    }

    // 🔒 Limites gerais de mobs
    @EventHandler
    public void aoSpawnarMob(CreatureSpawnEvent event) {
        Entity e = event.getEntity();
        Chunk chunk = e.getLocation().getChunk();
        String tipo = e.getType().name();
        int limite = getConfig().getInt("limites-chunk.mobs." + tipo, -1);

        if (limite > 0 && contarEntidadesDoTipo(chunk, tipo) >= limite) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void aoInteragir(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getItem() == null) return;

        Material item = event.getItem().getType();
        String nome = item.name();
        Player p = event.getPlayer();
        Chunk chunk = p.getLocation().getChunk();

        if (getConfig().contains("limites-chunk.blocos." + nome)) {
            int limite = getConfig().getInt("limites-chunk.blocos." + nome);
            if (contarEntidadesDoTipo(chunk, nome) >= limite) {
                event.setCancelled(true);
                p.sendMessage(ChatColor.RED + "⚠ Você atingiu o limite de " + limite + " para " + nome + " nesta área!");
            }
        }
    }

    @EventHandler
    public void aoColocarBloco(BlockPlaceEvent event) {
        Material mat = event.getBlockPlaced().getType();
        String nome = mat.name();
        Player p = event.getPlayer();
        Chunk chunk = event.getBlockPlaced().getChunk();

        if (getConfig().contains("limites-chunk.blocos." + nome)) {
            int limite = getConfig().getInt("limites-chunk.blocos." + nome);
            if (contarEntidadesDoTipo(chunk, nome) >= limite) {
                event.setCancelled(true);
                p.sendMessage(ChatColor.RED + "⚠ Você atingiu o limite de " + limite + " para " + nome + " nesta área!");
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (world.getEnvironment() == World.Environment.NETHER || world.getEnvironment() == World.Environment.THE_END) {
            for (Entity e : event.getChunk().getEntities()) {
                if (e instanceof LivingEntity && !(e instanceof Player)) {
                    e.remove();
                }
            }
        }
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        Chunk chunk = event.getWorld().getChunkAt(event.getBlocks().get(0).getLocation());
        int portalCount = contarPortais(chunk);
        if (portalCount >= 3) {
            event.setCancelled(true);
            for (Entity e : event.getWorld().getNearbyEntities(chunk.getBlock(8, 64, 8).getLocation(), 10, 10, 10)) {
                if (e instanceof Player p) {
                    p.sendMessage(ChatColor.RED + "⚠ Este chunk já atingiu o limite máximo de 3 portais!");
                }
            }
        }
    }

    private int contarPortais(Chunk chunk) {
        int count = 0;
        for (Block b : getChunkBlocks(chunk)) {
            if (b.getType() == Material.NETHER_PORTAL || b.getType() == Material.END_PORTAL) {
                count++;
            }
        }
        return count;
    }

    private List<Block> getChunkBlocks(Chunk chunk) {
        List<Block> blocks = new java.util.ArrayList<>();
        int bx = chunk.getX() << 4;
        int bz = chunk.getZ() << 4;
        World world = chunk.getWorld();

        for (int x = bx; x < bx + 16; x++)
            for (int z = bz; z < bz + 16; z++)
                for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++)
                    blocks.add(world.getBlockAt(x, y, z));
        return blocks;
    }

    private int contarEntidadesDoTipo(Chunk chunk, String tipo) {
        int count = 0;
        for (Entity e : chunk.getEntities()) {
            if (e.getType().name().equals(tipo)) count++;
        }
        return count;
    }

    private void broadcast(String msg) {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(msg));
        Bukkit.getConsoleSender().sendMessage(ChatColor.stripColor(msg));
    }

    private String msg(String key) {
        String m = messages.getString(key, "");
        return ChatColor.translateAlternateColorCodes('&', m == null ? "" : m);
    }

    private void sendDiscordLog(String content) {
        if (webhook == null || webhook.isEmpty()) return;
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                URL url = new URL(webhook);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);
                String json = "{\"content\":\"" + content + "\"}";
                try (OutputStream os = con.getOutputStream()) {
                    os.write(json.getBytes());
                }
                con.getInputStream().close();
                con.disconnect();
            } catch (Exception ex) {
                getLogger().warning("Erro ao enviar log para Discord: " + ex.getMessage());
            }
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "Apenas operadores podem usar este comando.");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("gclear")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.YELLOW + "Use: /gclear <tudo|mobs|itens|reload>");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "tudo" -> {
                    int r = limparTudo();
                    broadcast("🧹 Limpeza concluída: " + r + " entidades removidas!");
                }
                case "reload" -> {
                    reloadConfig();
                    carregarConfig();
                    messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
                    sender.sendMessage(ChatColor.GREEN + "✅ Config recarregada!");
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("gclear") && args.length == 1)
            return Arrays.asList("tudo", "mobs", "itens", "reload");
        return null;
    }
}
