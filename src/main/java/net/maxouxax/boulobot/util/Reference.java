package net.maxouxax.boulobot.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public enum Reference {

    EmbedFooter("BouloBOT - Amazingly powerful"),
    WebsiteURL("https://lyorine.com"),
    EmbedIcon("https://static.lyorine.com/boulobot/BouloBOT_f-min.png"),
    DiscordBanner("http://image.noelshack.com/fichiers/2019/01/4/1546534189-lyorinediscordbanner.png"),
    RulesEmbedThumbnail("https://png.icons8.com/rules/color/1600"),
    RulesEmbedThumbnailF("http://image.noelshack.com/fichiers/2019/01/4/1546536705-iconfinder-browser-1055104.png"),
    AttentionWebhookIconURL("http://image.noelshack.com/fichiers/2019/01/4/1546539623-iconfinder-caution-1055096.png"),
    AttentionWebhookFooterIconURL("http://image.noelshack.com/fichiers/2019/01/4/1546539791-iconfinder-arrow-down-1055120.png"),
    RankThumbnail("https://static.lyorine.com/boulobot/RankWebhookCircle.png"),

    RulesWebhookURL("https://discordapp.com/api/webhooks/530439612569485322/3nHhJyjQ1RdySolrz6uq87WW7TtTMP-DKPTyE9i2TSXp1DZJprPKdcvX_ZOWV1_AwpCC"),
    AttentionWebhookURL("https://discordapp.com/api/webhooks/530407569634885662/_OsubMEC7ul-ixlKP5VdLAWE3CQwgU9vaIjNRoubxragZaVmCKrP6g4DyEBgsDvP-21_"),
    GradesWebhookURL("https://discordapp.com/api/webhooks/541348235873353729/wyLdi7xbBkpDvxcwUXIBgMtHz3RKgmcpRP-PKXLdv5in7aic6g-dNvlCxKrV7niK3KQC"),

    GuildID("529310963816595467"),
    CheckEmoteID("530393425762320419"),
    RulesTextChannelID("530439257534365710"),
    NotifTextChannelID("678644069437669386"),
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
