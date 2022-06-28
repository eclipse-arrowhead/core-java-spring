package eu.arrowhead.core.translator.services.translator.common;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class TranslatorSetupDTO implements Serializable {
	private static final long serialVersionUID = 3919207845125510215L;
  private final InterfaceDTO in;
  private final InterfaceDTO out;

  public TranslatorSetupDTO(InterfaceDTO in, InterfaceDTO out) {
    this.in = in;
    this.out = out;
  }

  public InterfaceDTO getIn() {
    return in;
  }

  public InterfaceDTO getOut() {
    return out;
  }
}
