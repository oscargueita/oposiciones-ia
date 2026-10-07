package com.examprep.voice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SayTtsServiceTest {

  @Test
  void simpleWavDuration() {
    assertThat(SayTtsService.WavUtil.durationSec(TestAudio.wav(2.0))).isEqualTo(2.0);
  }

  @Test
  void wavDurationWithSayStyleExtraChunk() {
    assertThat(SayTtsService.WavUtil.durationSec(TestAudio.wavConChunkExtra(8.5, "FLLR")))
        .isCloseTo(8.5, org.assertj.core.api.Assertions.within(0.001));
  }
}
