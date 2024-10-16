package kr.co.miniIntra;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class MiniController {

	@Autowired
	private MiniMapper mapper;
	
	@GetMapping("/")
	public String home() {
		return "redirect:/login";
	}
	
	@GetMapping("/login")
	public String login() {
		return "/login";
	}
	
	@PostMapping("/login")
	public String loginOk(SawonVo svo,HttpSession session) {
		int level=mapper.isSawon(svo.getSabun(), svo.getPwd());
		
		if(level>0) {
			session.setAttribute("sabun", svo.getSabun());
			session.setAttribute("level", level);
			
			// 사원의 소속 부서 => 세션 변수에 저장
			switch(svo.getSabun().substring(0,3)) {
				case "s01":session.setAttribute("depart", "총무부"); break;
				case "s02":session.setAttribute("depart", "자재부"); break;
				case "s03":session.setAttribute("depart", "영업부"); break;
				case "s04":session.setAttribute("depart", "감사부"); break;
			}
			return "redirect:/main";
		} else {
			
			return "redirect:/login?err=1";
		}
	}
	
	@GetMapping("/main")
	public String getMain() {
		return "/main";
	}
	
	@GetMapping("/list")
	public String list(HttpServletRequest request,Model model) {
		
		int grp=Integer.parseInt(request.getParameter("grp"));
		int page=request.getParameter("page")==null?1:Integer.parseInt(request.getParameter("page"));
		int index=(page-1)*10;
		int p=(page-1)/10;
		int pstart=p*10+1;
		int pend=pstart+9;
		int chong=mapper.getChong(grp);
		if(pend>chong) {
			pend=chong;
		}
		
		model.addAttribute("page",page);
		model.addAttribute("pstart",pstart);
		model.addAttribute("pend",pend);
		model.addAttribute("chong",chong);
		
		ArrayList<BoardVo> list=mapper.list(grp,index);
		
		model.addAttribute("bvo",list);
		
		return "/list";
	}
	
	@GetMapping("/write")
	public String getWrite() {
		return "/write";
	}
	
	@PostMapping("/write")
	public String postWrite(BoardVo bvo,HttpSession session) {
		
		bvo.setSabun(session.getAttribute("sabun").toString());
		
		mapper.writeOk(bvo);
		return "redirect:list?grp="+bvo.getGrp();
	}
	
	@GetMapping("/content")
	public String getContent(@Param("id") int id,
			@Param("grp") int grp,
			@Param("page") int page, Model model) {

		BoardVo bvo=mapper.content(id);
		bvo.setContent(bvo.getContent().replace("\r\n", "<br>"));
		model.addAttribute("bvo",bvo);
		model.addAttribute("page",page);
		model.addAttribute("grp",grp);
		
		return "/content";
	}
	
	@GetMapping("/delete")
	public String delete(@Param("id") int id,
			@Param("grp") int grp,
			@Param("page") int page) {
		mapper.delete(id);
		
		return "redirect:list?page="+page+"&grp="+grp;
	}
	
	@GetMapping("/update")
	public String update(@Param("id") int id,
			@Param("grp") int grp,
			@Param("page") int page, Model model) {
		
		BoardVo bvo=mapper.content(id);
		model.addAttribute("bvo",bvo);
		model.addAttribute("grp",grp);
		model.addAttribute("page",page);
		
		return "update";
	}
	
	@PostMapping("/update")
	public String update() {
		return null;
	}
}
