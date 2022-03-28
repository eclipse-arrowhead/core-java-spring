package eu.arrowhead.core.translator.services.translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.stereotype.Service;

import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.translator.services.translator.common.TranslatorHubDTO;
import eu.arrowhead.core.translator.services.translator.common.TranslatorSetupDTO;
import eu.arrowhead.core.translator.services.translator.common.Translation.Protocol;

@Service
public class TranslatorService {
  // =================================================================================================
  // members
  private final Logger logger = LogManager.getLogger(TranslatorService.class);
  private final Map<Integer, TranslatorHub> hubs = new HashMap<>();

  // =================================================================================================
  // methods
  // -------------------------------------------------------------------------------------------------
  public void start() {
    logger.info("Starting Translator Service");
  }

  // -------------------------------------------------------------------------------------------------
  public TranslatorHubDTO createTranslatorHub(TranslatorSetupDTO setup) {
    int id = checkSetupDTO(setup);
    if (hubs.containsKey(id))
      throw new ArrowheadException("This Translator Hub already exists", HttpStatus.SC_CONFLICT);
    TranslatorHub hub = new TranslatorHub(id, setup);
    hubs.put(id, hub);
    return hub.getDTO();
  }

  // -------------------------------------------------------------------------------------------------
  public TranslatorHubDTO getTranslatorHubDTO(int id) {
    if (!hubs.containsKey(id))
      throw new ArrowheadException(String.format("Translator Hub id %d does NOT exists", id), HttpStatus.SC_CONFLICT);
    return hubs.get(id).getDTO();
  }

  // -------------------------------------------------------------------------------------------------
  public void deleteHub(int id) {
    if (!hubs.containsKey(id))
      throw new ArrowheadException(String.format("Translator Hub id %d does NOT exists", id), HttpStatus.SC_CONFLICT);
    // Get hub
    TranslatorHub hub = hubs.get(id);
    // remove it
    hubs.remove(id);

  }

  // -------------------------------------------------------------------------------------------------
  public ArrayList<TranslatorHubDTO> getHubsList() {
    ArrayList<TranslatorHubDTO> list = new ArrayList<TranslatorHubDTO>();
    hubs.entrySet().forEach((entry) -> {
      list.add(entry.getValue().getDTO());
    });
    return list;
  }

  // =================================================================================================
  // assistant methods
  // -------------------------------------------------------------------------------------------------
  private int checkSetupDTO(TranslatorSetupDTO setup) {
    if (setup == null)
      throw new ArrowheadException("Empty TranslatorSetupDTO", HttpStatus.SC_BAD_REQUEST);
    if (setup.getIn() == null)
      throw new ArrowheadException("Empty In Interface", HttpStatus.SC_BAD_REQUEST);
    if (setup.getIn().getProtocol() == null || setup.getOut().getProtocol() == Protocol.UNKOWN)
      throw new ArrowheadException("Empty In Interface Protocol", HttpStatus.SC_BAD_REQUEST);
    if (setup.getOut() == null)
      throw new ArrowheadException("Empty Out Interface", HttpStatus.SC_BAD_REQUEST);
    if (setup.getOut().getIp() == null)
      throw new ArrowheadException("Empty Out Interface Ip Address", HttpStatus.SC_BAD_REQUEST);
    if (setup.getOut().getPort() < 1 || setup.getIn().getPort() > 65535)
      throw new ArrowheadException("Wrong Out Interface Port number", HttpStatus.SC_BAD_REQUEST);
    if (setup.getOut().getProtocol() == null || setup.getOut().getProtocol() == Protocol.UNKOWN)
      throw new ArrowheadException("Empty Out Interface Protocol", HttpStatus.SC_BAD_REQUEST);
    return (setup.getIn().getProtocol() + setup.getOut().getIp() + setup.getOut().getPort()
        + setup.getOut().getProtocol()).hashCode();
  }
}
