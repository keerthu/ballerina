import ballerina.lang.system;
import ballerina.lang.jsons;
import ballerina.net.http;

@http:configuration {basePath:"/ecommerceservice"}
service<http> Ecommerce {
    @http:resourceConfig {
        methods:["GET"],
        path:"/products/{productId}/{regId}"
    }
    resource productsInfo1 (http:Request req, http:Response res, string productId, string regId) {
        string orderId = req.getHeader("X-ORDER-ID");
        system:println("Order ID " + orderId);
        system:println("Product ID " + productId);
        system:println("Reg ID " + regId);
        json responseJson = {"X-ORDER-ID":orderId, "ProductID":productId, "RegID":regId};
        system:println(jsons:toString(responseJson));
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products2/{productId}/{regId}/item"
    }
    resource productsInfo2 (http:Request req, http:Response res, string productId, string regId) {
        json responseJson;
        system:println("Product ID " + productId);
        system:println("Reg ID " + regId);
        responseJson = {"Template":"T2", "ProductID":productId, "RegID":regId};
        system:println(jsons:toString(responseJson));
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products3/{productId}/{regId}/*"
    }
    resource productsInfo3 (http:Request req, http:Response res, string productId, string regId) {
        json responseJson;
        system:println("Product ID " + productId);
        system:println("Reg ID " + regId);
        responseJson = {"Template":"T3", "ProductID":productId, "RegID":regId};
        system:println(jsons:toString(responseJson));
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products/{productId}"
    }
    resource productsInfo4 (http:Request req, http:Response res, string productId) {
        json responseJson;
        map params = req.getQueryParams();
        string rID;
        rID, _ = (string)params.regID;
        system:println("Product ID " + productId);
        system:println("Reg ID " + rID);
        responseJson = {"Template":"T4", "ProductID":productId, "RegID":rID};
        system:println(jsons:toString(responseJson));
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products"
    }
    resource productsInfo6 (http:Request req, http:Response res) {
        json responseJson;
        map params = req.getQueryParams();
        string prdID;
        string rID;
        prdID, _ = (string)params.prodID;
        rID, _ = (string)params.regID;
        system:println ("Product ID " + prdID);
        system:println ("Reg ID " + rID);
        responseJson = {"Template":"T6", "ProductID":prdID, "RegID":rID};
        system:println (jsons:toString (responseJson));
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products5/{productId}/reg"
    }
    resource productsInfo5 (http:Request req, http:Response res, string productId) {
        json responseJson;
        map params = req.getQueryParams();
        string rID;
        rID, _ = (string)params.regID;
        system:println("Product ID " + productId);
        system:println("Reg ID " + rID);
        responseJson = {"Template":"T5", "ProductID":productId, "RegID":rID};
        system:println(jsons:toString(responseJson));
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        path:""
    }
    resource echo1 (http:Request req, http:Response res) {
        json responseJson = {"echo11":"echo11"};
        res.setJsonPayload(responseJson);
        res.send();
    }
}

@http:configuration {
    basePath:"/options"
}
service<http> echo111 {

    @http:resourceConfig {
        methods:["POST", "UPDATE"],
        path : "/test"
    }
    resource productsInfo99 (http:Request req, http:Response res) {
        res.send();
    }

    @http:resourceConfig {
        methods:["OPTIONS"],
        path : "/hi"
    }
    resource productsOptions (http:Request req, http:Response res) {
        json responseJson = {"echo":"wso2"};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET", "PUT"],
        path : "/test"
    }
    resource productsInfo98 (http:Request req, http:Response res) {
        res.send();

    }

    @http:resourceConfig {
        methods:["GET"],
        path : "/getme"
    }
    resource productsGet (http:Request req, http:Response res) {
        json responseJson = {"echo":"get"};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["POST"],
        path : "/post"
    }
    resource productsPOST (http:Request req, http:Response res) {
        json responseJson = {"echo":"post"};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["PUT"],
        path : "/put"
    }
    resource productsPUT (http:Request req, http:Response res) {
        json responseJson = {"echo":"put"};
        res.setJsonPayload(responseJson);
        res.send();
    }
}

@http:configuration {
    basePath:"/noResource"
}
service<http> echo112 {
}