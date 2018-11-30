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

package ca.qc.ircm.maxquant2saint.maxquant;

import static org.junit.Assert.assertEquals;

import ca.qc.ircm.maxquant2saint.test.config.TestAnnotations;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestAnnotations
public class MaxquantConfigurationTest {
  @Inject
  private MaxquantConfiguration configuration;

  @Test
  public void defaultValues() throws Throwable {
    assertEquals("Protein IDs", configuration.getHeaders().getProteinIds());
    assertEquals("Gene names", configuration.getHeaders().getGeneNames());
    assertEquals("LFQ intensity (.*)", configuration.getHeaders().getLfq());
    assertEquals("MS/MS count (.*)", configuration.getHeaders().getMsmsCount());
    assertEquals("Intensity (.*)", configuration.getHeaders().getIntensity());
    assertEquals("Peptides (.*)", configuration.getHeaders().getPeptides());
    assertEquals("Razor + unique peptides (.*)", configuration.getHeaders().getRazorPeptides());
    assertEquals("Unique peptides (.*)", configuration.getHeaders().getUniquePeptides());
    assertEquals("Sequence coverage (.*) [%]", configuration.getHeaders().getCoverage());
  }
}
