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
	public ArrayList<SawonVo> getSawon(String depart);
	public void memoOk(MemoVo mvo);
	public ArrayList<MemoVo> getSend(String sabun, int seIndex, int pageSize);
	public ArrayList<MemoVo> getRe(String sabun, int index, int pageSize);
	public int getReChong(String sabun, int pageSize);
	public int getSeChong(String sabun, int pageSize);
	public MemoVo getMemo(int id);
}
