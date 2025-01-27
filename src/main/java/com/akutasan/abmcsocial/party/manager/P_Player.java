package com.akutasan.abmcsocial.party.manager;

import com.akutasan.abmcsocial.ABMCSocial;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class P_Player {
    // Main Management File, majority should be self-explanatory
    private final ProxiedPlayer leader;
    private final List<ProxiedPlayer> invitations;
    private final List<ProxiedPlayer> players;

    public P_Player(ProxiedPlayer leader){
        this.leader = leader;
        this.players = new ArrayList<>();
        this.invitations = new ArrayList<>();
    }

    public boolean isLeader(ProxiedPlayer p) {
        return this.leader == p;
    }

    public List<ProxiedPlayer> getPlayers() {
        return this.players;
    }

    public ProxiedPlayer getLeader()
    {
        return this.leader;
    }

    public List<ProxiedPlayer> getInvitations()
    {
        return this.invitations;
    }

    public boolean hasRequest(ProxiedPlayer p)
    {
        return this.invitations.contains(p);
    }

    public boolean isInParty(ProxiedPlayer p)
    {
        if (isLeader(p)) {
            return true;
        }
        return this.players.contains(p);
    }

    public boolean addPlayer(ProxiedPlayer p)
    {
        if ((!this.players.contains(p)) && (this.invitations.contains(p)))
        {
            this.players.add(p);
            this.invitations.remove(p);
            return true;
        }
        return false;
    }

    public boolean removePlayer(ProxiedPlayer p) {
        if (this.players.contains(p)) {
            this.players.remove(p);
            return true;
        } else {
            return false;
        }
    }

    public void removeInvite(ProxiedPlayer p)
    {
        this.invitations.remove(p);
    }

    public ServerInfo getServer()
    {
        return this.leader.getServer().getInfo();
    }

    // Invite command with in chat click components
    public void invite(final ProxiedPlayer p) {
        this.invitations.add(p);
        p.sendMessage(new TextComponent(ABMCSocial.partyprefix + "§a" + getLeader().getName() + " §ahas invited §eyou §ato §ahis §aParty!"));
        TextComponent accept = new TextComponent(ABMCSocial.partyprefix + "§7Click §e§lhere §7to §a§laccept!");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept " + getLeader().getName()));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§a/party accept §6" + getLeader().getName()).create()));
        TextComponent deny = new TextComponent(ABMCSocial.partyprefix + "§7Click §e§lhere §7to §c§ldecline!");
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party deny " + getLeader().getName()));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§c/party deny §6"+getLeader().getName()).create()));
        p.sendMessage(accept);
        p.sendMessage(deny);
        ProxyServer.getInstance().getScheduler().schedule(ABMCSocial.getInstance(), () -> {
            if (P_Player.this.hasRequest(p))
            {
                P_Player.this.invitations.remove(p);
                p.sendMessage(new TextComponent(ABMCSocial.partyprefix + "§cYour invitation §chas §cexpired!"));
                P_Player.this.getLeader().sendMessage(new TextComponent(ABMCSocial.partyprefix + "§cThe invitation to §e" + p.getName() + " §chas expired!"));
            }
            P_Player.this.start(p);
        }, 5L, TimeUnit.MINUTES);
    }

    // Two minute timer once only one party member is in party
    private void start(final ProxiedPlayer p) {
        ProxyServer.getInstance().getScheduler().schedule(ABMCSocial.getInstance(), () -> {
            P_Player party = P_Manager.getParty(p);
            if ((party != null) && (party.getPlayers().size() == 0))
            {
                P_Manager.deleteParty(p);
                party.getLeader().sendMessage(new TextComponent(ABMCSocial.partyprefix + "§cThe Party §cwas dissolved because of too §cfew §cmembers!"));
                p.sendMessage(new TextComponent(ABMCSocial.partyprefix + "§cThe Party §cwas §cdissolved!"));
            }
        }, 2L, TimeUnit.MINUTES);
    }

    public static String getRankCol(ProxiedPlayer player) {
        return "§e";
    }
}
