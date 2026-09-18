package hotel.web;

import hotel.model.Room;
import hotel.model.Guest;

public class JsonUtil {

    public static String escape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c <= '\u001F') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    public static String quoted(String s) {
        return "\"" + escape(s) + "\"";
    }

    public static String roomToJson(Room r) {
        return "{\"roomNo\":" + r.getRoomNo() + 
               ",\"roomType\":" + quoted(r.getRoomType()) + 
               ",\"basePrice\":" + r.getBasePrice() + 
               ",\"available\":" + r.isAvailable() + "}";
    }

    public static String guestToJson(Guest g) {
        return "{\"guestId\":" + g.getGuestId() + 
               ",\"name\":" + quoted(g.getName()) + 
               ",\"idProof\":" + quoted(g.getIdProof()) + 
               ",\"contact\":" + quoted(g.getContact()) + 
               ",\"loyaltyTier\":" + quoted(g.getLoyaltyTier()) + 
               ",\"bookingCount\":" + g.getBookingCount() + "}";
    }
}
