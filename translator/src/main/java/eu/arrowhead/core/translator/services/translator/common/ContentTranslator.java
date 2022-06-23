package eu.arrowhead.core.translator.services.translator.common;

import com.google.gson.Gson;
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import com.upokecenter.cbor.CBORObject;
import org.json.JSONObject;
import org.json.XML;

import eu.arrowhead.core.translator.services.translator.common.Translation.ContentType;

public class ContentTranslator {
    // CBOR
    public static String CBOR2JSON(String cbor) {
        int len = cbor.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(cbor.charAt(i), 16) << 4)
                    + Character.digit(cbor.charAt(i + 1), 16));
        }
        return CBORObject.DecodeSequenceFromBytes(bytes)[0].ToJSONString();
    }

    public static String CBOR2TOML(String cbor) {
        return JSON2TOML(CBOR2JSON(cbor));
    }

    public static String CBOR2XML(String cbor) {
        return JSON2XML(CBOR2JSON(cbor));
    }

    // JSON
    public static String JSON2CBOR(String json) {
        byte[] bytes = CBORObject.FromJSONString(new JSONObject(json).toString(10)).EncodeToBytes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02X", bytes[i]));
        }
        return "" + sb.toString();
    }

    public static String JSON2TOML(String json) {
        TomlWriter tomlWriter = new TomlWriter.Builder()
                .indentValuesBy(2)
                .indentTablesBy(4)
                .padArrayDelimitersBy(3)
                .build();
        return tomlWriter.write(new JSONObject(json));
    }

    public static String JSON2XML(String json) {
        return XML.toString(new JSONObject(json));
    }

    // TOML
    public static String TOML2CBOR(String toml) {
        return JSON2CBOR(TOML2JSON(toml));
    }

    public static String TOML2JSON(String toml) {
        Toml t = new Toml().read(toml);
        return new Gson().toJson(new Gson().toJsonTree(t).getAsJsonObject().get("values"));
    }

    public static String TOML2XML(String toml) {
        return JSON2XML(TOML2JSON(toml));
    }

    // XML
    public static String XML2CBOR(String xml) {
        return JSON2CBOR(XML2JSON(xml));
    }

    public static String XML2JSON(String xml) {
        return XML.toJSONObject(xml, true).toString();
    }

    public static String XML2TOML(String xml) {
        return JSON2TOML(XML2JSON(xml));
    }

    public static byte[] translate(ContentType to, ContentType from, byte[] payload) {
        try {
            String content = new String(payload);

            // CBOR
            if (from == ContentType.CBOR && to == ContentType.JSON) {
                return CBOR2JSON(content).getBytes();
            }
            if (from == ContentType.CBOR && to == ContentType.TEXT) {
                return CBOR2TOML(content).getBytes();
            }
            if (from == ContentType.CBOR && to == ContentType.XML) {
                return CBOR2XML(content).getBytes();
            }

            // JSON
            if (from == ContentType.JSON && to == ContentType.CBOR) {
                return JSON2CBOR(content).getBytes();
            }
            if (from == ContentType.JSON && to == ContentType.TEXT) {
                return JSON2TOML(content).getBytes();
            }
            if (from == ContentType.JSON && to == ContentType.XML) {
                return JSON2XML(content).getBytes();
            }

            // TEXT
            if (from == ContentType.TEXT && to == ContentType.CBOR) {
                return TOML2CBOR(content).getBytes();
            }
            if (from == ContentType.TEXT && to == ContentType.JSON) {
                return TOML2JSON(content).getBytes();
            }
            if (from == ContentType.TEXT && to == ContentType.XML) {
                return TOML2XML(content).getBytes();
            }

            // XML
            if (from == ContentType.XML && to == ContentType.CBOR) {
                return XML2CBOR(content).getBytes();
            }
            if (from == ContentType.XML && to == ContentType.JSON) {
                return XML2JSON(content).getBytes();
            }
            if (from == ContentType.XML && to == ContentType.TEXT) {
                return XML2TOML(content).getBytes();
            }

        } catch (Exception ex) {
            // Ignore, it will return default.
        }

        return payload;
    }
}
