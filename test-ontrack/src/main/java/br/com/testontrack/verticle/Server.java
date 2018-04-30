package br.com.testontrack.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.LinkedList;
import java.util.List;


public class Server extends AbstractVerticle {

  private MongoClient mongoClient;

  @Override
  public void start() throws Exception {
		super.start();

    mongoClient = MongoClient.createShared(vertx, new JsonObject().put("db_name", "ontrack"));

    loadData(mongoClient);

    Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create());

    router.get("/clientes").handler(ctx -> {
      mongoClient.find("clientes", new JsonObject(), lookup -> {
        if (lookup.failed()) {
          ctx.fail(500);
          return;
        }

        final JsonArray json = new JsonArray();

        for (JsonObject o : lookup.result()) {
          json.add(o);
        }

        ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        ctx.response().end(json.encode());
      });
    });

    router.get("/clientes/:id").handler(ctx -> {
      mongoClient.findOne("clientes", new JsonObject().put("_id", ctx.request().getParam("id")), null, lookup -> {
        if (lookup.failed()) {
          ctx.fail(500);
          return;
        }

        JsonObject cliente = lookup.result();

        if (cliente == null) {
          ctx.fail(404);
        } else {
          ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
          ctx.response().end(cliente.encode());
        }
      });
    });

    router.post("/clientes").handler(ctx -> {
      JsonObject newcliente = ctx.getBodyAsJson();

      mongoClient.findOne("clientes", new JsonObject().put("nome", newcliente.getString("nome")), null, lookup -> {
        if (lookup.failed()) {
          ctx.fail(500);
          return;
        }

        JsonObject cliente = lookup.result();

        if (cliente != null) {
          ctx.fail(500);
        } else {
          mongoClient.insert("clientes", newcliente, insert -> { 
            if (insert.failed()) {
              ctx.fail(500);
              return;
            }
            newcliente.put("_id", insert.result());

            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            ctx.response().end(newcliente.encode());
          });
        }
      });
    });

    router.put("/clientes/:id").handler(ctx -> {
      mongoClient.findOne("clientes", new JsonObject().put("_id", ctx.request().getParam("id")), null, lookup -> {
        if (lookup.failed()) {
          ctx.fail(500);
          return;
        }

        JsonObject cliente = lookup.result();

        if (cliente == null) {
          ctx.fail(404);
        } else {

          JsonObject update = ctx.getBodyAsJson();

          cliente.put("clienteId", update.getString("clienteId"));
          cliente.put("nome", update.getString("nome"));
          cliente.put("cpf", update.getString("cpf"));

          mongoClient.replace("clientes", new JsonObject().put("_id", ctx.request().getParam("id")), cliente, replace -> {
            if (replace.failed()) {
              ctx.fail(500);
              return;
            }

            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            ctx.response().end(cliente.encode());
          });
        }
      });
    });

    router.delete("/clientes/:id").handler(ctx -> {
      mongoClient.findOne("clientes", new JsonObject().put("_id", ctx.request().getParam("id")), null, lookup -> {
        if (lookup.failed()) {
          ctx.fail(500);
          return;
        }

        JsonObject cliente = lookup.result();

        if (cliente == null) {
          ctx.fail(404);
        } else {

          mongoClient.remove("clientes", new JsonObject().put("_id", ctx.request().getParam("id")), remove -> {
            if (remove.failed()) {
              ctx.fail(500);
              return;
            }

            ctx.response().setStatusCode(204);
            ctx.response().end();
          });
        }
      });
    });

    router.route().handler(StaticHandler.create());

    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }

  private void loadData(MongoClient db) {
    db.dropCollection("clientes", drop -> {
      if (drop.failed()) {
        throw new RuntimeException(drop.cause());
      }

      List<JsonObject> clientes = new LinkedList<>();

      clientes.add(new JsonObject()
    		  .put("clienteId", "1")
              .put("nome", "Teste")
              .put("cpf", "000.000.000-00"));

      for (JsonObject cliente : clientes) {
        db.insert("clientes", cliente, res -> {
          System.out.println("inserted " + cliente.encode());
        });
      }
    });
  }
}
