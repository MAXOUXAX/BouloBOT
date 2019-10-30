package net.maxx.boulobot.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public enum Reference {

    EmbedFooter("BouloBOT - Deserved by Lyo. Inc"),
    WebsiteURL("https://lyorine.com"),
    EmbedIcon("https://image.noelshack.com/fichiers/2019/20/5/1558109488-boulobot-f.png"),
    DiscordBanner("http://image.noelshack.com/fichiers/2019/01/4/1546534189-lyorinediscordbanner.png"),
    RulesEmbedThumbnail("https://png.icons8.com/rules/color/1600"),
    RulesEmbedThumbnailF("http://image.noelshack.com/fichiers/2019/01/4/1546536705-iconfinder-browser-1055104.png"),
    AttentionWebhookIconURL("http://image.noelshack.com/fichiers/2019/01/4/1546539623-iconfinder-caution-1055096.png"),
    AttentionWebhookFooterIconURL("http://image.noelshack.com/fichiers/2019/01/4/1546539791-iconfinder-arrow-down-1055120.png"),
    StrawpollThumbnail("http://image.noelshack.com/fichiers/2019/01/4/1546551534-iconfinder-form2-1940831.png"),
    RankThumbnail("http://image.noelshack.com/fichiers/2019/05/6/1549133619-rankwebhookcircle.png"),
    BirthdayThumbnail("http://image.noelshack.com/fichiers/2019/04/6/1548536703-iconfinder-birthday-cake-2138192.png"),
    ConffetiImage("http://image.noelshack.com/fichiers/2019/04/6/1548536776-iconfinder-party-newyears-confetti-2817125.png"),
    BellImage("https://image.noelshack.com/fichiers/2019/10/7/1552242062-icons8-notification-96.png"),

    RulesWebhookURL("https://discordapp.com/api/webhooks/530439612569485322/3nHhJyjQ1RdySolrz6uq87WW7TtTMP-DKPTyE9i2TSXp1DZJprPKdcvX_ZOWV1_AwpCC"),
    AttentionWebhookURL("https://discordapp.com/api/webhooks/530407569634885662/_OsubMEC7ul-ixlKP5VdLAWE3CQwgU9vaIjNRoubxragZaVmCKrP6g4DyEBgsDvP-21_"),
    GradesWebhookURL("https://discordapp.com/api/webhooks/541348235873353729/wyLdi7xbBkpDvxcwUXIBgMtHz3RKgmcpRP-PKXLdv5in7aic6g-dNvlCxKrV7niK3KQC"),

    GuildID("529310963816595467"),
    CheckEmoteID("530393425762320419"),
    RulesTextChannelID("530439257534365710"),
    BCTextChannelID("529324645766397953"),
    NotifTextChannelID("541034954386309150"),
    RankTextChannelID("530411062864904212"),
    RulesRoleID("529317720366514196"),
    RescueRoleID("529319914385702932"),
    NotifRoleID("541035634685640715"),
    ChangelogRoleID("605516756533903412"),
    LyorineClientID("305789865847685121"),
    MaxClientID("260727839257526272");

    private String format = "EEE d MMM yyyy HH:mm:ss";

    Reference(String string) {
        this.string = string;
    }

    private String string;

    public String getString() {
        return string;
    }

    public String asDate() {
        return string+" • "+new SimpleDateFormat(format).format(new Date());
    }


}
