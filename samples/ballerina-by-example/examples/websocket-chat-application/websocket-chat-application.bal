import ballerina.lang.system;
import ballerina.lang.maps;
import ballerina.net.ws;

@ws:configuration {
    basePath: "/chat/ws",
    port:9090,
    idleTimeoutInSeconds: 3600
}
service<ws> ChatApp {

    map consMap = {};

    resource onOpen(ws:Connection conn) {
        broadcast(consMap, "New client connected");
        consMap[conn.getID()] = conn;
    }

    resource onTextMessage(ws:Connection con, ws:TextFrame frame) {
        system:println(frame.text);
        broadcast(consMap, frame.text);
    }

    resource onIdleTimeout(ws:Connection con) {
        // Connection is closed due to inactivity after 1 hour
        system:println("Idle timeout: " + con.getID());
        con.closeConnection(1000, "Closing connection due to inactivity in chat");
    }

    resource onClose(ws:Connection con, ws:CloseFrame frame) {
        maps:remove(consMap, con.getID());
        broadcast(consMap, "User left");
    }
}

function broadcast(map consMap, string text) {
    string[] conKeys = maps:keys(consMap);
    int len = lengthof conKeys;
    int i = 0;
    while (i < len) {
        var con, _ = (ws:Connection) consMap[conKeys[i]];
        con.pushText(text);
        i = i + 1;
    }
}
