package hotel.web;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import hotel.ReservationManager;
import hotel.DatabaseManager;
import hotel.model.Room;
import hotel.model.Guest;

import java.net.InetSocketAddress;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.net.URLDecoder;
import java.util.concurrent.Executors;

public class WebServer {

    public static void main(String[] args) {
        System.out.println("Starting Hotel Booking System Web Server...");

        File webDir = new File("web");
        if (!webDir.exists() || !webDir.isDirectory()) {
            System.err.println("Error: 'web' directory not found in the current working directory.");
            System.exit(1);
        }

        DatabaseManager.initializeDatabase();

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

            server.createContext("/", WebServer::staticFileHandler);
            server.createContext("/api/rooms", WebServer::handleRooms);
            server.createContext("/api/rooms/update", WebServer::handleRoomUpdate);
            server.createContext("/api/rooms/delete", WebServer::handleRoomDelete);
            server.createContext("/api/guests", WebServer::handleGuests);
            server.createContext("/api/bookings", WebServer::handleBookings);
            server.createContext("/api/bookings/cancel", WebServer::handleBookingCancel);

            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();

            System.out.println("Server started at http://localhost:8080");
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }

    private static void staticFileHandler(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            System.out.println("[WEB] " + method + " " + path);

            if (!method.equals("GET")) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            if (path.equals("/")) {
                path = "/index.html";
            }

            Path webRoot = Paths.get("web").toAbsolutePath().normalize();
            Path requestedFile = webRoot.resolve(path.substring(1)).normalize();

            File file = requestedFile.toFile();
            if (!file.exists() || file.isDirectory()) {
                sendError(exchange, 404, "File Not Found");
                return;
            }

            Path canonicalWebRoot = webRoot.toRealPath();
            Path canonicalRequestedFile = requestedFile.toRealPath();

            if (!canonicalRequestedFile.startsWith(canonicalWebRoot)) {
                sendError(exchange, 403, "Forbidden: Path Traversal Detected");
                return;
            }

            String contentType = "text/plain";
            if (path.endsWith(".html")) contentType = "text/html;charset=UTF-8";
            else if (path.endsWith(".css")) contentType = "text/css;charset=UTF-8";
            else if (path.endsWith(".js")) contentType = "application/javascript;charset=UTF-8";

            byte[] bytes = Files.readAllBytes(requestedFile);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (Exception e) {
            System.out.println("[WEB][ERROR] " + e.getMessage());
            sendError(exchange, 500, "Internal Server Error");
        }
    }

    private static String getAllRoomsJson() throws SQLException {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT room_no, room_type, base_price, available FROM rooms ORDER BY room_no";
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            boolean first = true;
            while (rs.next()) {
                if (!first) {
                    json.append(",");
                }
                json.append("{")
                    .append("\"roomNo\":").append(rs.getInt("room_no")).append(",")
                    .append("\"roomType\":").append(JsonUtil.quoted(rs.getString("room_type"))).append(",")
                    .append("\"basePrice\":").append(rs.getDouble("base_price")).append(",")
                    .append("\"available\":").append(rs.getInt("available") == 1)
                    .append("}");
                first = false;
            }
        }
        json.append("]");
        return json.toString();
    }

    private static String getAllBookingsJson() throws SQLException {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT booking_id, guest_id, room_no, check_in, check_out, bill FROM bookings ORDER BY booking_id DESC";
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            boolean first = true;
            while (rs.next()) {
                if (!first) {
                    json.append(",");
                }
                json.append("{")
                    .append("\"bookingId\":").append(rs.getInt("booking_id")).append(",")
                    .append("\"guestId\":").append(rs.getInt("guest_id")).append(",")
                    .append("\"roomNo\":").append(rs.getInt("room_no")).append(",")
                    .append("\"checkIn\":").append(JsonUtil.quoted(rs.getString("check_in"))).append(",")
                    .append("\"checkOut\":").append(JsonUtil.quoted(rs.getString("check_out"))).append(",")
                    .append("\"bill\":").append(rs.getDouble("bill"))
                    .append("}");
                first = false;
            }
        }
        json.append("]");
        return json.toString();
    }

    private static Room getRoomForVerification(int roomNo) {
        return DatabaseManager.getRoom(roomNo);
    }

    private static Map<String, String> parseFormData(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.isEmpty()) {
            return map;
        }
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            try {
                String key = URLDecoder.decode(kv[0], "UTF-8");
                String value = kv.length > 1 ? URLDecoder.decode(kv[1], "UTF-8") : "";
                map.put(key, value);
            } catch (UnsupportedEncodingException e) {
                // Ignore
            }
        }
        return map;
    }

    private static void sendJson(HttpExchange ex, int status, String json) {
        try {
            byte[] bytes = json.getBytes("UTF-8");
            ex.getResponseHeaders().set("Content-Type", "application/json;charset=UTF-8");
            ex.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException e) {
            System.out.println("[WEB][ERROR] Failed to send JSON: " + e.getMessage());
        }
    }

    private static void sendSuccess(HttpExchange ex, String message) {
        sendJson(ex, 200, "{\"success\":true,\"message\":" + JsonUtil.quoted(message) + "}");
    }

    private static void sendError(HttpExchange ex, int status, String message) {
        sendJson(ex, status, "{\"success\":false,\"message\":" + JsonUtil.quoted(message) + "}");
    }

    private static void sendData(HttpExchange ex, String dataArrayJson) {
        sendJson(ex, 200, "{\"success\":true,\"data\":" + dataArrayJson + "}");
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private static void handleRooms(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            System.out.println("[WEB] " + method + " /api/rooms");

            if (method.equals("GET")) {
                sendData(exchange, getAllRoomsJson());
            } else if (method.equals("POST")) {
                Map<String, String> data = parseFormData(readBody(exchange));
                if (!data.containsKey("roomNo") || !data.containsKey("roomType") || !data.containsKey("basePrice")) {
                    sendError(exchange, 400, "Missing required fields");
                    return;
                }
                
                int roomNo = Integer.parseInt(data.get("roomNo"));
                String roomType = data.get("roomType");
                double basePrice = Double.parseDouble(data.get("basePrice"));
                
                if (!Double.isFinite(basePrice)) {
                    sendError(exchange, 400, "Invalid base price");
                    return;
                }

                Room existing = getRoomForVerification(roomNo);
                if (existing != null) {
                    sendError(exchange, 400, "Room already exists");
                    return;
                }

                ReservationManager.addRoom(roomNo, roomType, basePrice);

                Room added = getRoomForVerification(roomNo);
                if (added != null) {
                    sendSuccess(exchange, "Room added successfully.");
                } else {
                    sendError(exchange, 500, "Failed to add room.");
                }
            } else {
                sendError(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            System.out.println("[WEB][ERROR] " + e.getMessage());
            sendError(exchange, 500, "Internal Server Error");
        }
    }

    private static void handleRoomUpdate(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            System.out.println("[WEB] " + method + " /api/rooms/update");

            if (!method.equals("POST")) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            Map<String, String> data = parseFormData(readBody(exchange));
            if (!data.containsKey("roomNo") || !data.containsKey("roomType") || !data.containsKey("basePrice")) {
                sendError(exchange, 400, "Missing required fields");
                return;
            }

            int roomNo = Integer.parseInt(data.get("roomNo"));
            String roomType = data.get("roomType");
            double basePrice = Double.parseDouble(data.get("basePrice"));

            if (!Double.isFinite(basePrice)) {
                sendError(exchange, 400, "Invalid base price");
                return;
            }

            ReservationManager.updateRoom(roomNo, roomType, basePrice);

            Room updated = getRoomForVerification(roomNo);
            if (updated != null && updated.getRoomType().equals(roomType) && updated.getBasePrice() == basePrice) {
                sendSuccess(exchange, "Room updated successfully.");
            } else {
                sendError(exchange, 500, "Failed to update room.");
            }
        } catch (Exception e) {
            System.out.println("[WEB][ERROR] " + e.getMessage());
            sendError(exchange, 500, "Internal Server Error");
        }
    }

    private static void handleRoomDelete(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            System.out.println("[WEB] " + method + " /api/rooms/delete");

            if (!method.equals("POST")) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            Map<String, String> data = parseFormData(readBody(exchange));
            if (!data.containsKey("roomNo")) {
                sendError(exchange, 400, "Missing roomNo");
                return;
            }

            int roomNo = Integer.parseInt(data.get("roomNo"));
            Room existing = getRoomForVerification(roomNo);
            if (existing == null) {
                sendError(exchange, 400, "Room does not exist");
                return;
            }

            ReservationManager.removeRoom(roomNo);

            Room after = getRoomForVerification(roomNo);
            if (after == null) {
                sendSuccess(exchange, "Room deleted successfully.");
            } else {
                sendError(exchange, 500, "Failed to delete room. It may have booking history or be currently booked.");
            }
        } catch (Exception e) {
            System.out.println("[WEB][ERROR] " + e.getMessage());
            sendError(exchange, 500, "Internal Server Error");
        }
    }

    private static void handleGuests(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            System.out.println("[WEB] " + method + " /api/guests");

            if (method.equals("GET")) {
                ArrayList<Guest> guests = DatabaseManager.getAllGuests();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < guests.size(); i++) {
                    if (i > 0) json.append(",");
                    json.append(JsonUtil.guestToJson(guests.get(i)));
                }
                json.append("]");
                sendData(exchange, json.toString());
            } else if (method.equals("POST")) {
                Map<String, String> data = parseFormData(readBody(exchange));
                if (!data.containsKey("name") || !data.containsKey("idProof") || !data.containsKey("contact")) {
                    sendError(exchange, 400, "Missing required fields");
                    return;
                }

                String name = data.get("name");
                String idProof = data.get("idProof");
                String contact = data.get("contact");

                boolean success = DatabaseManager.addGuest(name, idProof, contact);
                if (success) {
                    sendSuccess(exchange, "Guest registered successfully.");
                } else {
                    sendError(exchange, 400, "Failed to register guest.");
                }
            } else {
                sendError(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            System.out.println("[WEB][ERROR] " + e.getMessage());
            sendError(exchange, 500, "Internal Server Error");
        }
    }

    private static void handleBookings(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            System.out.println("[WEB] " + method + " /api/bookings");

            if (method.equals("GET")) {
                sendData(exchange, getAllBookingsJson());
            } else if (method.equals("POST")) {
                Map<String, String> data = parseFormData(readBody(exchange));
                if (!data.containsKey("guestId") || !data.containsKey("roomNo") || !data.containsKey("checkIn") || !data.containsKey("checkOut")) {
                    sendError(exchange, 400, "Missing required fields");
                    return;
                }

                int guestId = Integer.parseInt(data.get("guestId"));
                int roomNo = Integer.parseInt(data.get("roomNo"));
                String checkIn = data.get("checkIn");
                String checkOut = data.get("checkOut");

                String[] inParts = checkIn.split("-");
                String convertedCheckIn = inParts[2] + "-" + inParts[1] + "-" + inParts[0];

                String[] outParts = checkOut.split("-");
                String convertedCheckOut = outParts[2] + "-" + outParts[1] + "-" + outParts[0];

                boolean success = ReservationManager.bookRoom(guestId, roomNo, convertedCheckIn, convertedCheckOut);
                if (success) {
                    sendSuccess(exchange, "Booking created successfully.");
                } else {
                    sendError(exchange, 400, "Booking failed. Room may not be available or dates may be invalid.");
                }
            } else {
                sendError(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            System.out.println("[WEB][ERROR] " + e.getMessage());
            sendError(exchange, 500, "Internal Server Error");
        }
    }

    private static void handleBookingCancel(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            System.out.println("[WEB] " + method + " /api/bookings/cancel");

            if (!method.equals("POST")) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            Map<String, String> data = parseFormData(readBody(exchange));
            if (!data.containsKey("bookingId")) {
                sendError(exchange, 400, "Missing bookingId");
                return;
            }

            int bookingId = Integer.parseInt(data.get("bookingId"));
            
            boolean exists = false;
            String sql = "SELECT booking_id FROM bookings WHERE booking_id = ?";
            try (Connection conn = DatabaseManager.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, bookingId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        exists = true;
                    }
                }
            }

            if (!exists) {
                sendError(exchange, 400, "Booking does not exist");
                return;
            }

            ReservationManager.cancelBooking(bookingId);

            boolean stillExists = false;
            try (Connection conn = DatabaseManager.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, bookingId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        stillExists = true;
                    }
                }
            }

            if (!stillExists) {
                sendSuccess(exchange, "Booking cancelled successfully.");
            } else {
                sendError(exchange, 500, "Failed to cancel booking.");
            }
        } catch (Exception e) {
            System.out.println("[WEB][ERROR] " + e.getMessage());
            sendError(exchange, 500, "Internal Server Error");
        }
    }
}
