package net.ambitious.bvlion.batch2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ExecTimeEnum {
	FROM5(1),
	TO5(2),
	FROM1(3),
	TO1(4);

	private final int type;
}
