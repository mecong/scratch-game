package game.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import java.io.IOException;

public class CustomPrettyPrinter extends DefaultPrettyPrinter {

  public CustomPrettyPrinter() {
    _arrayIndenter = new DefaultIndenter("  ", "\n");
  }

  @Override
  public DefaultPrettyPrinter createInstance() {
    return new CustomPrettyPrinter();
  }

  @Override
  public void writeStartArray(JsonGenerator g) throws IOException {
    super.writeStartArray(g);
    if (!g.getOutputContext().inArray()) {
      _arrayIndenter.writeIndentation(g, _nesting);
    }
  }

  @Override
  public void writeArrayValueSeparator(JsonGenerator g) throws IOException {
    if (g.getOutputContext().inArray() && g.getOutputContext().getParent().inArray()) {
      g.writeRaw(", ");
    } else {
      super.writeArrayValueSeparator(g);
    }
  }

  @Override
  public void writeEndArray(JsonGenerator g, int nrOfValues) throws IOException {
    if (!g.getOutputContext().inArray()) {
      _arrayIndenter.writeIndentation(g, _nesting);
    }
    super.writeEndArray(g, nrOfValues);
  }
}
