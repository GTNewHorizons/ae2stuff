package net.bdew.ae2stuff;

@com.gtnewhorizon.gtnhlib.config.Config(modid = "ae2stuff", category = "client", filename = "ae2stuff")
@com.gtnewhorizon.gtnhlib.config.Config.LangKey("ae2stuff.config.client")
public class Config {

    @com.gtnewhorizon.gtnhlib.config.Config.DefaultFloat(1.0f)
    @com.gtnewhorizon.gtnhlib.config.Config.RangeFloat(min = 0.1f, max = 32.0f)
    @com.gtnewhorizon.gtnhlib.config.Config.Comment("Width of the links represented by dense cables displayed by the Network Visualisation Tool.")
    public static float visualiserWidthDense;

    @com.gtnewhorizon.gtnhlib.config.Config.DefaultFloat(1.0f)
    @com.gtnewhorizon.gtnhlib.config.Config.RangeFloat(min = 0.1f, max = 8.0f)
    @com.gtnewhorizon.gtnhlib.config.Config.Comment("Width of the links represented by normal cables displayed by the Network Visualisation Tool.")
    public static float visualiserWidthNormal;
}
