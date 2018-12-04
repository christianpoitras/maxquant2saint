package ca.qc.ircm.maxquant2saint;

import ca.qc.ircm.maxquant2saint.fasta.SequenceService;
import ca.qc.ircm.maxquant2saint.maxquant.Intensity;
import ca.qc.ircm.maxquant2saint.maxquant.MaxquantProteinGroup;
import ca.qc.ircm.maxquant2saint.maxquant.MaxquantService;
import ca.qc.ircm.maxquant2saint.saint.Interaction;
import ca.qc.ircm.maxquant2saint.saint.Prey;
import ca.qc.ircm.maxquant2saint.saint.SaintBaitWriter;
import ca.qc.ircm.maxquant2saint.saint.SaintInteractionWriter;
import ca.qc.ircm.maxquant2saint.saint.SaintPreyWriter;
import ca.qc.ircm.maxquant2saint.saint.Sample;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Converts MaxQuant protein groups file to SAINT input files.
 */
@Component
public class MaxquantToSaintConverter {
  private static final double DELTA = 0.0000000001;
  private static final String ELEMENT_SEPARATOR = ";";
  private static final Logger logger = LoggerFactory.getLogger(MaxquantToSaintConverter.class);
  @Inject
  private ConversionConfiguration configuration;
  @Inject
  private MaxquantService maxquantService;
  @Inject
  private SequenceService sequenceService;

  protected MaxquantToSaintConverter() {
  }

  /**
   * Convert MaxQuant protein groups file to SAINT input files.
   *
   * @param file
   *          MaxQuant protein groups file
   * @param fasta
   *          FASTA file used in MaxQuant search
   */
  public void convert(Path file, Path fasta) {
    Intensity intensity = configuration.getIntensity();
    Pattern baitPattern = Pattern.compile(configuration.getBait());
    Pattern controlPattern = Pattern.compile(configuration.getControl());
    Function<String, Boolean> isControl = sampleName -> {
      return controlPattern.matcher(sampleName).matches();
    };
    Function<String, String> baitName = sampleName -> {
      boolean control = isControl.apply(sampleName);
      Matcher baitMatcher = baitPattern.matcher(sampleName);
      return !control && baitMatcher.matches() ? baitMatcher.group(1) : sampleName;
    };
    Path baitFile = file.resolveSibling("bait.txt");
    Path preyFile = file.resolveSibling("prey.txt");
    Path interactionsFile = file.resolveSibling("interactions.txt");
    List<MaxquantProteinGroup> groups = maxquantService.proteinGroups(file, intensity);
    if (groups.isEmpty()) {
      logger.warn("No protein groups in file {}", file);
    }
    try (SaintBaitWriter writer = new SaintBaitWriter(Files.newBufferedWriter(baitFile))) {
      MaxquantProteinGroup group = groups.get(0);
      for (String sampleName : group.intensities.keySet()) {
        Sample sample = new Sample();
        sample.name = sampleName;
        sample.bait = baitName.apply(sampleName);
        sample.control = isControl.apply(sampleName);
        writer.writeSample(sample);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Could not write bait file " + baitFile, e);
    }
    try (SaintPreyWriter writer = new SaintPreyWriter(Files.newBufferedWriter(preyFile))) {
      final Map<String, Integer> lengths;
      final int averageLength;
      if (fasta != null) {
        lengths = sequenceService.sequencesLength(fasta);
        int tempAverageLength = (int) Math.round(groups.stream()
            .flatMap(group -> group.proteinIds.stream()).map(protein -> lengths.get(protein))
            .filter(value -> value != null).mapToInt(value -> value).average().orElse(0));
        if (tempAverageLength == 0) {
          tempAverageLength = (int) Math
              .round(lengths.values().stream().mapToInt(value -> value).average().orElse(0));
        }
        if (tempAverageLength == 0) {
          logger.warn("Cannot find length of any proteins");
        }
        averageLength = tempAverageLength;
      } else {
        logger.warn("Cannot find length of any proteins");
        lengths = new HashMap<>();
        averageLength = 0;
      }
      for (MaxquantProteinGroup group : groups) {
        int length = group.proteinIds.stream().map(protein -> lengths.get(protein))
            .filter(value -> value != null).mapToInt(value -> value).max().orElseGet(() -> {
              logger.info("using average length {} for protein group {}", averageLength,
                  group.proteinIds);
              return averageLength;
            });
        writer.writePrey(
            new Prey(group.proteinIds.stream().collect(Collectors.joining(ELEMENT_SEPARATOR)),
                length, group.geneNames.stream().collect(Collectors.joining(ELEMENT_SEPARATOR))));
      }
    } catch (IOException e) {
      throw new IllegalStateException("Could not write prey file " + preyFile, e);
    }
    try (SaintInteractionWriter writer =
        new SaintInteractionWriter(Files.newBufferedWriter(interactionsFile))) {
      for (MaxquantProteinGroup group : groups) {
        for (Map.Entry<String, Double> intensities : group.intensities.entrySet()) {
          if (Math.abs(intensities.getValue()) > DELTA) {
            Sample sample = new Sample();
            sample.name = intensities.getKey();
            sample.bait = baitName.apply(sample.name);
            sample.control = isControl.apply(sample.name);
            writer.writeInteraction(new Interaction(sample,
                new Prey(group.proteinIds.stream().collect(Collectors.joining(ELEMENT_SEPARATOR))),
                intensities.getValue()));
          }
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Could not write interaction file " + interactionsFile, e);
    }
  }
}
