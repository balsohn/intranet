package kr.co.miniIntra;

import lombok.Data;

@Data
public class MemoVo {
	private int id, state;
	private String seSabun, reSabun, title, content, fname, writeday;
}
