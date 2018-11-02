package org.sonatype.repository.conan.internal;

import java.io.IOException;
import java.net.URL;

import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Status;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

@Singleton
public class ConanPythonContext
    extends ComponentSupport
{
  private final Context context;

  public ConanPythonContext() {
    try {
      context = initialiseContext();
    }
    catch (IOException e) {
      throw new RuntimeException("Unable to evaluate python file", e);
    }
  }

  private Context initialiseContext() throws IOException {
    Context ctx = Context.newBuilder().allowAllAccess(true).allowIO(true).build();

    Value python = ctx.getBindings("python");
    python.putMember(Payload.class.getSimpleName(), Payload.class);
    python.putMember(Handler.class.getSimpleName(), Handler.class);
    python.putMember(HttpResponses.class.getSimpleName(), HttpResponses.class);
    python.putMember(Response.class.getSimpleName(), Response.class);
    python.putMember("ResponseBuilder", Response.Builder.class);
    python.putMember(Status.class.getSimpleName(), Status.class);
    python.putMember(org.sonatype.nexus.repository.view.Context.class.getSimpleName(), org.sonatype.nexus.repository.view.Context.class);

    evaluateFile(ctx, "/hosted/HostedHandlers.py");
    return ctx;
  }

  public Handler getPingHandler() {
    Value clazz = context.eval("python", "PingHandler");
    return clazz.newInstance().as(Handler.class);
  }

  private void evaluateFile(final Context context, final String filename) throws IOException {
    URL systemResource = getClass().getClassLoader().getResource(filename);
    //File file = new File(systemResource.getFile());
    Source source = Source.newBuilder("python", systemResource).build();
    context.eval(source);
  }
}
