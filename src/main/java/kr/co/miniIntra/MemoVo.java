package kr.co.miniIntra;

import java.util.List;

import lombok.Data;

@Data
public class MemoVo {
	private int id, state;
	private String seSabun, reSabun, title, content, fname, writeday;
	
	// DelMemo
	private List memoIds;
	private String type;
}
