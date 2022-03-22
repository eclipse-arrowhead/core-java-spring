package eu.arrowhead.core.translator.services.translator.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class TranslatorHubDTO implements Serializable {
  private static final long serialVersionUID = 3919207845123510215L;
  private final int id;
  private final InterfaceDTO in;
  private final InterfaceDTO out;

  public TranslatorHubDTO(int id, InterfaceDTO in, InterfaceDTO out) {
    this.id = id;
    this.in = in;
    this.out = out;
  }

  public int getId() {
    return id;
  }

  public InterfaceDTO getIn() {
    return in;
  }

  public InterfaceDTO getOut() {
    return out;
  }
}
