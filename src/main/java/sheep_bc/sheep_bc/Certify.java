package sheep_bc.sheep_bc;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import javax.security.auth.login.LoginException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Certify extends  JavaPlugin implements Listener,CommandExecutor {

    public Server server;
    static JDA jda;
    public static Map<String,Player> list = new HashMap<>();
    public static List<Player> player_list = new ArrayList<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this,this);
        getCommand("인증").setExecutor(this);
        getCommand("인증취소").setExecutor(this);
        server = getServer();


        try {
            jda = JDABuilder.createDefault(getSetting("BotToken")).build();
            jda.getPresence().setActivity(Activity.playing("인증"));
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
            jda.addEventListener(new Discord());
        } catch (Exception e) {
            getServer().getLogger().config("디코봇 실패");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player))return false;
        Player player = (Player) sender;
        if(label.equals("인증")){
            if(isCertify(player)){
                player.sendMessage("이미 인증하였습니다.");
            }
            else {
                String code = getCode(player);
                String message = "\n \n"+ ChatColor.WHITE+"[ 인증봇 ] "+ChatColor.YELLOW+"플레이어님의 "
                        +ChatColor.GREEN+"인증"+ChatColor.YELLOW+"코드는 "+
                        ChatColor.WHITE+"["+ChatColor.RED+code+ChatColor.WHITE+"] "+ChatColor.YELLOW+"입니다."+"\n \n ";
                player.sendMessage(message);
            }
        }
        if(label.equals("인증취소") & player.isOp()){
            if(args.length == 0){
                player.sendMessage("/인증취소 [플레이어]");
            }
            else {
                Player player1 = getServer().getPlayer(args[0]);
                if(player1 == null){
                    player.sendMessage("플레이어를 찾지못하였습니다.");
                }
                if(!isCertify(player1)){
                    player.sendMessage("인증하지 않은 플레이어입니다.");
                }
                else {
                    String tag = player1.getScoreboardTags().stream().filter(s -> s.startsWith("Discord:")).findFirst().get();
                    player1.removeScoreboardTag(tag);
                    player.sendMessage(player1.getName()+" 인증 취소 완료");
                }
            }
        }
        return false;
    }

    @EventHandler
    public void move(PlayerMoveEvent event){
        if(!isCertify(event.getPlayer())){
            Player player = event.getPlayer();
            Location set = new Location(player.getWorld(),0.5,88,129.5);
            if(player.getLocation().distance(set) > 0.05){
                player.teleport(set);
            }
            else {
                event.setCancelled(true);
            }

        }
    }

    @EventHandler
    public void join(PlayerJoinEvent event){
        if(!isCertify(event.getPlayer())){
            new BukkitRunnable() {
                @Override
                public void run() {
                    event.getPlayer().kickPlayer("인증을 하지 않았습니다.");
                }
            }.runTaskLater(this,20*60);
        }
    }

    @Override
    public void onDisable() {
        jda.shutdownNow();
    }

    public static boolean isCertify(Player player){
        List<String> list = player.getScoreboardTags().stream().toList();
        for(String s:list){
            if(s.startsWith("Discord:")) return true;
        }
        return false;
    }

    private String getCode(Player player) {
        if(player_list.contains(player)){
            return list.keySet()
                    .stream()
                    .filter(key -> player.equals(list.get(key)))
                    .findFirst().get();
        }
        String code = makeCode();
        player_list.add(player);
        list.put(code,player);
        return code;
    }

    public String makeCode(){
        String es = getSetting("Element");
        String[] list = es.split("");
        String s ="";
        int num = Integer.parseInt(getSetting("Size"));
        for (int i =0;i<num;i++){
            s += list[(int)(Math.random()*(list.length-1))];
        }
        return s;
    }

    public static String getSetting(String key){
        try {
            Map<String, Object> propMap = new Yaml().load(new FileReader("Certify_Setting.yml"));
            return (String) propMap.get(key);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
