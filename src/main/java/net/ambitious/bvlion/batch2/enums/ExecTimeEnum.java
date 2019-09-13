package net.ambitious.bvlion.batch2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ExecTimeEnum {
	FROM(1),
	TO(2);

	private final int type;
}
