package hotel.web;

import hotel.model.Room;
import hotel.model.Guest;

public class JsonUtil {

    public static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
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
