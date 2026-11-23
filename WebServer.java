import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Web layer for EventForge. It uses the SAME Java classes (Event, Allocation, Resource...).
// The browser page (index.html) talks to this server using simple URLs.
public class WebServer implements HttpHandler {
    static Event event = null;                                    // the event currently being viewed
    static ArrayList<Event> events = new ArrayList<Event>();      // ALL events
    static String lastMessage = "";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new WebServer());
        server.start();
        System.out.println("EventForge is running. Open http://localhost:8080 in your browser.");
        System.out.println("Press Ctrl + C here to stop the server.");
    }

    public void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String response = "";
        String contentType = "application/json";
        int status = 200;
        lastMessage = "";

        try {
            Map<String, String> p = parseQuery(ex.getRequestURI().getRawQuery());

            if (path.equals("/")) {
                response = new String(Files.readAllBytes(Paths.get("index.html")), StandardCharsets.UTF_8);
                contentType = "text/html";
            } else if (path.equals("/api/event")) {
                response = eventJson();
            } else if (path.equals("/api/create")) {
                String eventName = p.get("name");
                if (eventName == null || eventName.trim().equals("")) {
                    throw new IllegalArgumentException("Please enter an event name");
                }
                String budgetText = p.get("budget");
                if (budgetText == null || budgetText.trim().equals("")) {
                    throw new IllegalArgumentException("Please enter a budget amount");
                }
                event = new Event(eventName.trim(), p.get("date"), Double.parseDouble(budgetText));
                events.add(event);
                lastMessage = "Event created.";
                response = eventJson();
            } else if (path.equals("/api/demo")) {
                event = buildDemo();
                events.add(event);
                lastMessage = "Demo event loaded.";
                response = eventJson();
            } else if (path.equals("/api/select")) {
                int index = Integer.parseInt(p.get("index"));
                if (index < 0 || index >= events.size()) {
                    throw new IllegalArgumentException("Event not found");
                }
                event = events.get(index);
                response = eventJson();
            } else if (path.equals("/api/add")) {
                needEvent();
                event.addAllocation(p.get("role"), makeResource(p));
                lastMessage = "Resource added. Now assign a backup!";
                response = eventJson();
            } else if (path.equals("/api/backup")) {
                needEvent();
                event.assignBackup(Integer.parseInt(p.get("index")), makeResource(p));
                lastMessage = "Backup assigned.";
                response = eventJson();
            } else if (path.equals("/api/fail")) {
                needEvent();
                lastMessage = event.reportUnavailable(Integer.parseInt(p.get("index")));
                response = eventJson();
            } else if (path.equals("/api/activate")) {
                needEvent();
                event.activateBackup(Integer.parseInt(p.get("index")));
                lastMessage = "Backup activated!";
                response = eventJson();
            } else {
                status = 404;
                response = errorJson("Not found");
            }
        } catch (Exception e) {
            status = 400;
            contentType = "application/json";
            String msg = e.getMessage();
            if (msg == null) {
                msg = "Invalid input. Please check all fields.";
            }
            response = errorJson(msg);
        }

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        OutputStream os = ex.getResponseBody();
        os.write(bytes);
        os.close();
    }

    static void needEvent() {
        if (event == null) {
            throw new IllegalStateException("Create an event first.");
        }
    }

    // Reads ?a=1&b=2 into a Map (a Collections Framework example)
    static Map<String, String> parseQuery(String query) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        if (query == null) {
            return map;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String value = "";
            if (kv.length == 2) {
                value = URLDecoder.decode(kv[1], "UTF-8");
            }
            map.put(kv[0], value);
        }
        return map;
    }

    // Polymorphism again: returns a Resource, real object depends on the type chosen
    static Resource makeResource(Map<String, String> p) {
        String type = p.get("type");
        String name = p.get("name");
        double cost = Double.parseDouble(p.get("cost"));
        String extra = p.get("extra");

        if (type.equals("venue")) {
            return new Venue(name, cost, Integer.parseInt(extra));
        } else if (type.equals("vendor")) {
            return new Vendor(name, cost, extra);
        } else if (type.equals("volunteer")) {
            return new Volunteer(name, cost, extra);
        } else if (type.equals("equipment")) {
            return new Equipment(name, cost, Integer.parseInt(p.get("qty")));
        } else if (type.equals("decoration")) {
            return new Decoration(name, cost, extra);
        } else {
            throw new IllegalArgumentException("Unknown resource type");
        }
    }

    static Event buildDemo() throws Exception {
        Event e = new Event("Tech Fest 2026", "15-Dec-2026", 200000);
        e.addAllocation("Main Venue", new Venue("Main Auditorium", 50000, 500));
        e.assignBackup(0, new Venue("Open Air Arena", 40000, 400));
        e.addAllocation("Catering", new Vendor("Spice Caterers", 60000, "Catering"));
        e.assignBackup(1, new Vendor("Royal Foods", 55000, "Catering"));
        e.addAllocation("Registration Desk", new Volunteer("Anita", 500, "Registration"));
        e.assignBackup(2, new Volunteer("Ravi", 500, "Registration"));
        e.addAllocation("Projectors", new Equipment("HD Projector", 2000, 4));
        e.assignBackup(3, new Equipment("Spare Projector", 2000, 2));
        e.addAllocation("Stage Decor", new Decoration("Floral Stage Setup", 15000, "Floral"));
        e.assignBackup(4, new Decoration("Balloon Arch Setup", 12000, "Balloon"));
        return e;
    }

    // ----- building JSON text by hand (so no extra libraries are needed) -----
    static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    static String errorJson(String msg) {
        return "{\"error\":\"" + esc(msg) + "\"}";
    }

    static String resourceJson(Resource r) {
        if (r == null) {
            return "null";
        }
        return "{\"type\":\"" + esc(r.getType()) + "\",\"name\":\"" + esc(r.getName())
                + "\",\"cost\":" + r.getCost() + ",\"available\":" + r.isAvailable() + "}";
    }

    static String allocationJson(Allocation a) {
        return "{\"role\":\"" + esc(a.getRole()) + "\",\"primary\":" + resourceJson(a.getPrimary())
                + ",\"backup\":" + resourceJson(a.getBackup())
                + ",\"backupActive\":" + a.isBackupActive()
                + ",\"cost\":" + a.getCommittedCost() + "}";
    }

    static String eventJson() {
        if (event == null) {
            return "{\"exists\":false,\"message\":\"" + esc(lastMessage) + "\"}";
        }
        String json = "{\"exists\":true";
        json = json + ",\"name\":\"" + esc(event.getName()) + "\"";
        json = json + ",\"date\":\"" + esc(event.getDate()) + "\"";
        json = json + ",\"budget\":" + event.getBudget();
        json = json + ",\"spent\":" + event.getTotalCost();
        json = json + ",\"left\":" + event.getRemainingBudget();
        json = json + ",\"status\":\"" + event.getStatus() + "\"";
        json = json + ",\"statusMessage\":\"" + esc(event.getStatus().getMessage()) + "\"";
        json = json + ",\"message\":\"" + esc(lastMessage) + "\"";
        json = json + ",\"current\":" + events.indexOf(event);
        json = json + ",\"eventNames\":[";
        for (int k = 0; k < events.size(); k++) {
            if (k > 0) {
                json = json + ",";
            }
            json = json + "\"" + esc(events.get(k).getName()) + "\"";
        }
        json = json + "]";
        json = json + ",\"items\":[";
        ArrayList<Allocation> list = event.getAllocations();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                json = json + ",";
            }
            json = json + allocationJson(list.get(i));
        }
        json = json + "]}";
        return json;
    }
}
