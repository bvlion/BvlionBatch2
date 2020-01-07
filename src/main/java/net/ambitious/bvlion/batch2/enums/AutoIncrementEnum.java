package net.ambitious.bvlion.batch2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AutoIncrementEnum {
	SLEEP_MUSIC(1);

	private final int type;
}
