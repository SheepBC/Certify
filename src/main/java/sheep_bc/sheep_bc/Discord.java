package sheep_bc.sheep_bc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Objects;

public class Discord extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event){

        String[] message  = event.getMessage().getContentRaw().split(" ");
        if(message[0].equals("!인증")){

            if(wasCertify(event.getAuthor(), event.getJDA())){
                event.getChannel().sendMessage("이미 인증한 계정입니다.").queue();
                //코드 출력됨
                return;
            }

            if(message.length == 1){
                event.getChannel().sendMessage("!인증 [코드]").queue();
            }
            else {
                String code = message[1];
                if(Certify.list.containsKey(code)){

                    Guild guild = event.getJDA().getGuildById(Certify.getSetting("GuildID"));//yml 에서 길드 가져오기
                    User user = event.getAuthor();//유저 가져오기

                    Player player = Certify.list.get(code);//플레이어 가져오기
                    player.addScoreboardTag("Discord:"+user.getId());//스코어보드에 디스코드 아이디 추가

                    //플레이어에게 메세지 보내기
                    String msg = "\n \n"+ChatColor.WHITE+"[ 인증봇 ] "+ChatColor.GREEN+"인증 "+ChatColor.YELLOW+"완료되었습니다. 감사합니다.\n \n ";
                    player.sendMessage(msg);

                    //소리재생
                    player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 20, 0);

                    //리스트에서 제거
                    Certify.list.remove(code);
                    Certify.player_list.remove(player);

                    //디스코드에 메세지 보내기
                    EmbedBuilder embed = new EmbedBuilder();
                    String url = "https://minotar.net/avatar/"+player.getName()+"/100.png";
                    embed.setTitle("[ 인증해주셔서 감사합니다 ]");
                    embed.setImage(url);
                    embed.addField("마인크래프트: "+player.getName(), "디스코드: "+user.getName(), true);
                    embed.addField(LocalDate.now()+"/"+ LocalTime.now(), player.getUniqueId().toString(), true);
                    event.getChannel().sendMessageEmbeds(embed.build()).queue();

                    //디스코드에 역할 추가
                    try {
                        Objects.requireNonNull(guild).addRoleToMember(user, Objects.requireNonNull(guild.getRoleById(Objects.requireNonNull(Certify.getSetting("RoleID"))))).queue();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        }

    }

    public boolean wasCertify(User user, JDA jda){
        try {
            Guild guild = jda.getGuildById(Certify.getSetting("GuildID"));
            Role role = guild.getRoleById(Certify.getSetting("RoleID"));
            Member member = guild.getMember(user);

            return member.getRoles().contains(role);
        }catch (Exception e){
            return false;
        }

    }

}
