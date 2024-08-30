package com.t8rin.psd.reader.parser;

public enum BlendMode {
    NORMAL("norm"),
    DISSOLVE("diss"),
    DARKEN("dark"),
    MULTIPLY("mul "),
    COLOR_BURN("idiv"),
    LINEAR_BURN("lbrn"),
    LIGHTEN("lite"),
    SCREEN("scrn"),
    COLOR_DODGE("div "),
    LINEAR_DODGE("lddg"),
    OVERLAY("over"),
    SOFT_LIGHT("sLit"),
    HARD_LIGHT("hLit"),
    VIVID_LIGHT("vLit"),
    LINEAR_LIGHT("lLit"),
    PIN_LIGHT("pLit"),
    HARD_MIX("hMix"),
    DIFFERENCE("diff"),
    EXCLUSION("smud"),
    HUE("hue "),
    SATURATION("sat "),
    COLOR("colr"),
    LUMINOSITY("lum "),
    PASS_THROUGH("pass");

    private final String name;

    BlendMode(String name) {
        this.name = name;
    }

    public static BlendMode getByName(String name) {
        for (BlendMode mode : values()) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }
        return null;
    }

}
