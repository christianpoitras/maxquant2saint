/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.maxquant2saint.saint;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

/**
 * Writes SAINT bait files.
 */
public class SaintBaitWriter implements Closeable {
  private static final String SEPARATOR = "\t";
  private static final String NEW_LINE = System.getProperty("line.separator");
  private final Writer writer;

  public SaintBaitWriter(Writer writer) {
    this.writer = writer;
  }

  /**
   * Writes sample in SAINT bait format.
   *
   * @param sample
   *          sample sample
   * @throws IOException
   *           could not write to writer
   */
  public void writeSample(Sample sample) throws IOException {
    writer.write(sample.name);
    writer.write(SEPARATOR);
    writer.write(Objects.toString(sample.bait, ""));
    writer.write(SEPARATOR);
    writer.write(sample.control ? "C" : "T");
    writer.write(NEW_LINE);
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }
}
