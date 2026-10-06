package com.oposiciones.voz;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SayTtsServiceTest {

  @Test
  void duracionWavSimple() {
    assertThat(SayTtsService.WavUtil.duracionSeg(TestAudio.wav(2.0))).isEqualTo(2.0);
  }

  @Test
  void duracionWavConChunkExtraEstiloSay() {
    assertThat(SayTtsService.WavUtil.duracionSeg(TestAudio.wavConChunkExtra(8.5, "FLLR")))
        .isCloseTo(8.5, org.assertj.core.api.Assertions.within(0.001));
  }
}
