package glorydark.bansystem.gui;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.Config;
import glorydark.bansystem.API;
import glorydark.bansystem.BindTask;
import glorydark.bansystem.EventListener;
import glorydark.bansystem.MainClass;

import java.util.*;

public class GuiListener implements Listener {
    public static final HashMap<Player, HashMap<Integer, GuiType>> UI_CACHE = new HashMap<>();
    public static final HashMap<Player, String> dealReport = new HashMap<>();
    public static void showFormWindow(Player player, FormWindow window, GuiType guiType) {
        UI_CACHE.computeIfAbsent(player, i -> new HashMap<>()).put(player.showFormWindow(window), guiType);
    }

    @EventHandler
    public void PlayerFormRespondedEvent(PlayerFormRespondedEvent event){
        Player p = event.getPlayer();
        FormWindow window = event.getWindow();
        if (p == null || window == null) {
            return;
        }
        GuiType guiType = UI_CACHE.containsKey(p) ? UI_CACHE.get(p).get(event.getFormID()) : null;
        if(guiType == null){
            return;
        }
        UI_CACHE.get(p).remove(event.getFormID());
        if (event.getResponse() == null) {
            return;
        }
        if (window instanceof FormWindowSimple) {
            this.formWindowSimpleOnClick(p, (FormWindowSimple) window, guiType);
        }
        if (window instanceof FormWindowCustom) {
            this.formWindowCustomOnClick(p, (FormWindowCustom) window, guiType);
        }
        if (window instanceof FormWindowModal) {
            this.formWindowModalOnClick(p, (FormWindowModal) window, guiType);
        }
    }
    private void formWindowSimpleOnClick(Player p, FormWindowSimple window, GuiType guiType) {
        if(window.getResponse() == null){ return; }
        int id = window.getResponse().getClickedButtonId();
        switch (guiType){
            case PlayerMain:
                switch (id) {
                    case 0:
                        GuiMain.showPlayerReportMenu(p);
                        break;
                    case 1:
                        GuiMain.showPlayerEmailMenu(p);
                        break;
                }
                break;
            case ManagerMain:
                switch (id){
                    case 0:
                        GuiMain.showManagerManagePlayerMenu(p);
                        break;
                    case 1:
                        GuiMain.showManagerReportListMenu(p);
                        break;
                    case 2:
                        GuiMain.showManagerBindPlayerMenu(p);
                        break;
                }
                break;
            case ManagerPlayerMinor:
                switch (id){
                    case 0:
                        GuiMain.showManagerBanMenu(p);
                        break;
                    case 1:
                        GuiMain.showManagerRemoveBanMenu(p);
                        break;
                    case 2:
                        GuiMain.showManagerKickMenu(p);
                        break;
                }
                break;
            case ManagerReportListMenu:
                Config config = new Config(MainClass.path+"/reportRecord.json",Config.JSON);
                List<String> strings = Arrays.asList(config.getKeys(false).toArray(new String[config.getKeys(false).size()]));
                String s = strings.get(window.getResponse().getClickedButtonId());
                GuiListener.dealReport.put(p,s);
                GuiMain.showManagerReportDealMenu(p,s);
                break;
            case ManagerBindPlayer:
                Player bind = Server.getInstance().getPlayer(window.getResponse().getClickedButton().getText());
                if(bind != null) {
                    BindTask.bindPlayers.put(p, bind);
                    p.setGamemode(1);
                    p.hidePlayer(bind);
                    for(Player player: Server.getInstance().getOnlinePlayers().values()){
                        player.hidePlayer(p);
                    }
                    p.teleportImmediate(p.getLocation());
                }
                break;
        }
    }

    private void formWindowCustomOnClick(Player p, FormWindowCustom window, GuiType guiType) {
        if(window.getResponse() == null){ return; }
        switch (guiType){
            case ManagerBanPlayer:
                if(window.getResponse().getInputResponse(0) != null && !(window.getResponse().getInputResponse(0).equals(""))) {
                    String player = window.getResponse().getInputResponse(0);
                    int year = (int) window.getResponse().getSliderResponse(2);
                    int month = (int) window.getResponse().getSliderResponse(3);
                    int day = (int) window.getResponse().getSliderResponse(4);
                    int hour = (int) window.getResponse().getSliderResponse(5);
                    int minute = (int) window.getResponse().getSliderResponse(6);
                    int second = (int) window.getResponse().getSliderResponse(7);
                    if(window.getResponse().getInputResponse(1) == null || window.getResponse().getInputResponse(1).equals("")) { GuiMain.showReturnMenu(p, "??????????????????!",GuiType.ReturnToManagerBanPlayer); return; }
                    API.banPlayer(player, window.getResponse().getInputResponse(1), year, month, day, hour, minute, second);
                }else{
                    GuiMain.showReturnMenu(p, "????????????????????????!",GuiType.ReturnToManagerBanPlayer);
                }
                break;
            case ManagerKickPlayer:
                Player player = Server.getInstance().getPlayer(window.getResponse().getDropdownResponse(0).getElementContent());
                if(player != null){
                    String reason = window.getResponse().getInputResponse(1);
                    if(reason != null && !reason.equals("")) {
                        player.kick("\n [BanSystem] ??????????????????????????????????????????:"+"\n"+reason);
                        GuiMain.showReturnMenu(p, "??????????????????!",GuiType.ReturnToManagerKickPlayer);
                    }else{
                        GuiMain.showReturnMenu(p, "??????????????????!",GuiType.ReturnToManagerKickPlayer);
                    }
                }else{
                    GuiMain.showReturnMenu(p, "???????????????!",GuiType.ReturnToManagerKickPlayer);
                }
                break;
            case ManagerRemoveBanPlayer:
                String string = window.getResponse().getDropdownResponse(0).getElementContent();
                if(string != null && !string.equals("")){
                    API.unbanPlayer(string);
                    GuiMain.showReturnMenu(p, "??????????????????"+string+"!",GuiType.ReturnToManagerRemoveBanPlayer);
                }else{
                    GuiMain.showReturnMenu(p, "??????????????????????????????!",GuiType.ReturnToManagerRemoveBanPlayer);
                }
                break;
            case PlayerReport:
                String reported = window.getResponse().getInputResponse(0);
                if(reported != null && !reported.equals("")){
                    if(window.getResponse().getInputResponse(2) != null && !window.getResponse().getInputResponse(2).equals("")) {
                        API.updateReport(p.getName(), reported, window.getResponse().getDropdownResponse(1).getElementContent(), window.getResponse().getInputResponse(2), window.getResponse().getToggleResponse(3));
                        GuiMain.showReturnMenu(p, "??????????????????!",GuiType.ReturnToPlayerReport);
                    }else{
                        GuiMain.showReturnMenu(p, "??????????????????????????????!",GuiType.ReturnToPlayerReport);
                    }
                }else{
                    GuiMain.showReturnMenu(p, "????????????????????????!",GuiType.ReturnToPlayerReport);
                }
                break;
            case ManagerReportDealMenu:
                if(dealReport.containsKey(p)) {
                    String name = dealReport.get(p);
                    dealReport.remove(p);
                    if (name != null && !name.equals("")) {
                        String reason = window.getResponse().getInputResponse(2);
                        if (reason == null || reason.equals("")) {
                            GuiMain.showReturnMenu(p, "??????????????????????????????!",GuiType.ManagerReportListMenu);
                            return;
                        }
                        Config config = new Config(MainClass.path + "/reportRecord.json", Config.JSON);
                        Config cfg = new Config(MainClass.path + "/dealMethod.yml");
                        String playerName = (String)((Map<String,Object>)config.get(name)).get("????????????");
                        Player dealObject = Server.getInstance().getPlayer(playerName);
                        String deal = window.getResponse().getDropdownResponse(1).getElementContent();
                        if (deal.equals("????????????")) {
                            dealObject.kick("\n [BanSystem] ??????????????????????????????????????????:" + "\n" + reason);
                        } else {
                            String[] strings = deal.split("\\|");
                            if (cfg.exists(deal)) {
                                switch (strings[0]) {
                                    case "??????":
                                        int year = cfg.getInt(deal + ".year");
                                        int month = cfg.getInt(deal + ".month");
                                        int day = cfg.getInt(deal + ".day");
                                        int hour = cfg.getInt(deal + ".hour");
                                        int minute = cfg.getInt(deal + ".minute");
                                        int second = cfg.getInt(deal + ".second");
                                        API.banPlayer(playerName,reason,year,month,day,hour,minute,second);
                                        break;
                                }
                            }
                        }
                        config.remove(name);
                        config.save();
                        GuiMain.showReturnMenu(p, "????????????!",GuiType.ManagerReportListMenu);
                    }
                }else{
                    GuiMain.showReturnMenu(p, "????????????????????????????????????!",GuiType.ManagerReportListMenu);
                    return;
                }
                break;
        }
    }

    private void formWindowModalOnClick(Player p, FormWindowModal window, GuiType guiType) {
        if(window.getResponse() == null){ return; }
        switch (guiType){
            case ReturnToPlayerEmail:
                if(window.getResponse().getClickedButtonId() == 0) {
                    GuiMain.showPlayerEmailMenu(p);
                }
                break;
            case ReturnToManagerBanPlayer:
                if(window.getResponse().getClickedButtonId() == 0) {
                    GuiMain.showManagerBanMenu(p);
                }
                break;
            case ReturnToManagerKickPlayer:
                if(window.getResponse().getClickedButtonId() == 0) {
                    GuiMain.showManagerKickMenu(p);
                }
                break;
            case ReturnToManagerRemoveBanPlayer:
                if(window.getResponse().getClickedButtonId() == 0) {
                    GuiMain.showManagerRemoveBanMenu(p);
                }
                break;
            case ReturnToManagerReportListMenu:
                if(window.getResponse().getClickedButtonId() == 0) {
                    GuiMain.showManagerReportListMenu(p);
                }
                break;
            case ReturnToPlayerReport:
                if(window.getResponse().getClickedButtonId() == 0) {
                    GuiMain.showPlayerReportMenu(p);
                }
                break;
        }
    }
}
