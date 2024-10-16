package kr.co.miniIntra;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MiniMapper {

	public int isSawon(String sabun, String pwd);
	public ArrayList<BoardVo> list(int grp, int index);
	public void writeOk(BoardVo bvo);
	public int getChong(int grp);
	public BoardVo content(int id);
	public void delete(int id);
}
