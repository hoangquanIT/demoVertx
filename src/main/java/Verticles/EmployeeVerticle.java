package Verticles;

import Models.Employee;
import io.netty.handler.codec.base64.Base64Decoder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Base64;

public class EmployeeVerticle extends AbstractVerticle {
    private HashMap employees = new HashMap();
    public void createExampleData()
    {
        employees.put(1,new Employee(1,"Mr Obama","Obama@gmail.com"));
        employees.put(2,new Employee(2,"Mr Donald Trump","Trump@gmail.com"));
        employees.put(3,new Employee(3,"Mr Putin","Putin@gmail.com"));
    }
    private void getAllEmployees(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type","application/json;charset=UTF-8");
        response.end(Json.encodePrettily(employees.values()));
    }

    private void getSortEmployees(RoutingContext routingContext){
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type","application/json;charset=UTF-8");
        //parameter lay tu nguoi dung
        String sort = routingContext.request().getParam("sort");
        if (sort == null){
            //Neu khong co thi cho loi API
            routingContext.response().setStatusCode(400).end();
        } else {
            //Dung ArrayList de chua cac key
            ArrayList<Integer> sortedKeys = new ArrayList<>(employees.keySet());
            //Mac dinh sap xep tand dan cua key
            Collections.sort(sortedKeys);
            //Neu sort la desc (giam dan)
            if (sort.equalsIgnoreCase("desc")){
                Collections.reverse(sortedKeys);
            }
            ArrayList sortedEmployees = new ArrayList();
            for (Integer key : sortedKeys) {
                sortedEmployees.add(employees.get(key));
            }
            response.end(Json.encodePrettily(sortedEmployees));
        }
    }

    private void getOneEmployee(RoutingContext routingContext){
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json;charset=utf-8");

        //Lay id tu URL
        String sid = routingContext.request().getParam("id");
        if (sid == null){
            routingContext.response().setStatusCode(400).end();
        } else {
            int id = Integer.parseInt(sid);
            Employee emp = (Employee)employees.get(id);

            response.end(Json.encodePrettily(emp));
        }
    }

    private void insertNewEmployee(RoutingContext routingContext){
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json;charset=utf-8");
        try{
            //routingContext.body() lay du lieu tu phia client voi ding dang Json
            //son.decodeValue((Buffer) routingContext.body(), Employee.class); -> dua Json do ve dang Employee
            Employee emp = Json.decodeValue( routingContext.getBody(), Employee.class);
            //dua vao HashMap
            employees.put(emp.getId(), emp);
            //Xuat ket qua la true cho client
            response.end("true");
        } catch (Exception ex){
            response.end(ex.getMessage());
        }
    }

    private void detectMotion(RoutingContext routingContext){
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "text/html")
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods","GET, POST, OPTIONS")
                .putHeader("Access-Control-Allow-Credentials", "true")
                .putHeader("Access-Control-Allow-Headers", "accept, authorization, content-type, email");
        System.out.println("motion detected");
        response.end("true");
//        try {
//            //String text = routingContext.request().getParam("test");
//
//
//
//        } catch (Exception ex){
//            response.end(ex.getMessage());
//        }
    }

    private void receiveImage(RoutingContext routingContext){
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "text/html")
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods","GET, POST, PUT, OPTIONS")
                .putHeader("Access-Control-Allow-Credentials", "true")
                .putHeader("Access-Control-Allow-Headers", "accept, authorization, content-type, email");

        int count = 1;
        for (FileUpload f : routingContext.fileUploads()) {
            System.out.println(count++);
            System.out.println(f.contentType());
            System.out.println(f.name());
            if (f.contentType().equalsIgnoreCase("image/jpeg")){
                BufferedImage bufferedImage = null;

                Buffer fileUploaded = routingContext.vertx().fileSystem().readFileBlocking(f.uploadedFileName());
                byte[] imageByte = fileUploaded.getBytes();
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
                    bufferedImage = ImageIO.read(bis);
                    bis.close();
                    System.out.println("Decoding bytes to image");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error while decoding bytes to image");
                    response.end("Error while decoding bytes to image");
                }

                File outputfile = new File("/home/quanhoang/Pictures/images/" + f.fileName() + ".jpeg");
                try {
                    ImageIO.write(bufferedImage, "jpeg", outputfile);
                    System.out.println("Saving image");

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error while saving image");
                    response.end("FAIL");
                }
            }
            else if (f.contentType().equalsIgnoreCase("application/xml")){
                BufferedInputStream bin = null;

                Buffer fileUploaded = routingContext.vertx().fileSystem().readFileBlocking(f.uploadedFileName());
                System.out.println(fileUploaded);
                byte[] bytes = fileUploaded.getBytes();
//                try {
//                    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
//                    bin = new BufferedInputStream(bis);
//                    bis.close();
//                    System.out.println("Decoding bytes to image");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    System.out.println("Error while decoding bytes to image");
//                    response.end("Error while decoding bytes to image");
//                }

//                File outputfile = new File("/home/quanhoang/Documents/profile/" + f.uploadedFileName() + ".xml");
//                FileOutputStream fout = null;
//                BufferedOutputStream bout = null;
//                try {
//                    fout = new FileOutputStream(outputfile);
//                    bout = new BufferedOutputStream(fout);
//
//                    bout.write(bytes);
//
//                    System.out.println("Saving file");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    System.out.println("Error while saving file");
//                    response.end("FAIL");
//                }
            }
            else {
                response.end("File Not Found");
            }
        }
        response.end("SUCCESS");

    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        //Khoi tao du lieu cho HashMap
        //createExampleData();
        Router router = Router.router(vertx);

        router.route().handler(CorsHandler.create("*")
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.PUT)
                .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
                .allowCredentials(true)
                .allowedHeader("x-requested-with")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Authorization")
                .allowedHeader("Access-Control-Allow-Methods")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("origin")
                .allowedHeader("accept")
                .allowedHeader("Content-Type"));

//        router.get("/api/employees").handler(this::getAllEmployees);
//        router.get("/api/employeessort").handler(this::getSortEmployees);
//        router.get("/api/employees/:id").handler(this::getOneEmployee);
//        //router.route("/api/employees*").handler(BodyHandler.create()); -> nhan cac du lieu tu client gui len. Neu thieu se bao loi
//        router.route("/api/employees*").handler(BodyHandler.create());
//        router.post("/api/employees").handler(this::insertNewEmployee);
        router.route().handler(BodyHandler.create());
        //router.route(HttpMethod.POST, "/check").handler(this::detectMotion);
        router.route(HttpMethod.POST, "/check/").handler(this::receiveImage);
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(config().getInteger("http.port", 8080),
                        result -> {
                            if (result.succeeded()) {
                                startPromise.complete();
                            } else {
                                startPromise.fail(result.cause());
                            }
                        }
                );
    }
}
