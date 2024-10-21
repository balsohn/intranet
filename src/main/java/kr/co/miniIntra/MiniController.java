package kr.co.miniIntra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

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
	
	@GetMapping("/myMemo")
	public String myMemo(HttpSession session, Model model,
	                     @RequestParam(defaultValue = "0") int page,
	                     @RequestParam(defaultValue = "0") int sentPage) {

	    if(session.getAttribute("sabun") == null) {
	        return "redirect:/login";
	    } else {
	        String sabun = session.getAttribute("sabun").toString();
	        int pageSize = 10;

	        // 받은 메모 페이징
	        int reIndex = page * pageSize;
	        ArrayList<MemoVo> mlist2 = mapper.getRe(sabun, reIndex, pageSize);
	        int reChong = mapper.getReChong(sabun, pageSize);

	        // 받은 메모 페이지 그룹 계산
	        int reCurrentGroup = (page / 10) + 1;
	        int reTotalGroup = (reChong + 9) / 10;
	        int reStartPage = (reCurrentGroup - 1) * 10;
	        int reEndPage = Math.min(reCurrentGroup * 10 - 1, reChong - 1);

	        model.addAttribute("mlist2", mlist2);
	        model.addAttribute("page", page);
	        model.addAttribute("reChong", reChong);
	        model.addAttribute("reCurrentGroup", reCurrentGroup);
	        model.addAttribute("reTotalGroup", reTotalGroup);
	        model.addAttribute("reStartPage", reStartPage);
	        model.addAttribute("reEndPage", reEndPage);

	        // 보낸 메모 페이징
	        int seIndex = sentPage * pageSize;
	        ArrayList<MemoVo> mlist1 = mapper.getSend(sabun, seIndex, pageSize);
	        int seChong = mapper.getSeChong(sabun, pageSize);

	        // 보낸 메모 페이지 그룹 계산
	        int seCurrentGroup = (sentPage / 10) + 1;
	        int seTotalGroup = (seChong + 9) / 10;
	        int seStartPage = (seCurrentGroup - 1) * 10;
	        int seEndPage = Math.min(seCurrentGroup * 10 - 1, seChong - 1);

	        model.addAttribute("mlist1", mlist1);
	        model.addAttribute("sentPage", sentPage);
	        model.addAttribute("seChong", seChong);
	        model.addAttribute("seCurrentGroup", seCurrentGroup);
	        model.addAttribute("seTotalGroup", seTotalGroup);
	        model.addAttribute("seStartPage", seStartPage);
	        model.addAttribute("seEndPage", seEndPage);

	        return "/myMemo";
	    }
	}
	
	@PostMapping("/myMemo")
	public String postMyMemo(MemoVo mvo,MultipartHttpServletRequest request) throws Exception {
		MultipartFile file=request.getFile("fname2");
		String dir=request.getServletContext().getRealPath("/uploads");
		Path path=Paths.get(dir);
		if(Files.notExists(path)) {
			Files.createDirectory(path);
		}
		
		if(!file.isEmpty()) {
			String fname=file.getOriginalFilename();
			path=Paths.get(dir,fname);
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			mvo.setFname(fname);
		}
		
		mapper.memoOk(mvo);
		
		return "/myMemo";
	}
	
	@GetMapping("/getSawon")
	public @ResponseBody ArrayList<SawonVo> getSawon(@Param("depart") String depart) {
		return mapper.getSawon(depart);
	}
	
	@GetMapping("/getMemo/{id}")
	public @ResponseBody MemoVo getMemo(@PathVariable int id) {
		return mapper.getMemo(id);
	}
	
	@PostMapping("/deleteMemos")
	public @ResponseBody ResponseEntity<Map<String, Boolean>> deleteMemo(@RequestBody MemoVo request) {
		boolean success = mapper.deleteMemo(request.getMemoIds());
		Map<String, Boolean> response=new HashMap<>();
		response.put("success", success);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/commute")
	public String commute() {
		return "/commute";
	}
	
	@GetMapping("/commute1")
	public @ResponseBody ResponseEntity<Map<String, String>> commute(HttpSession session) {
		String sabun=session.getAttribute("sabun").toString();
		String success=mapper.isWork(sabun);
		
		Map<String, String> response=new HashMap<>();
		if(!success.isEmpty()) {
			response.put("count", success);
		} else {
			response.put("count", "0");
		}
		return ResponseEntity.ok(response);
		
	}
	
	@PostMapping("/commute")
	public @ResponseBody ResponseEntity<Map<String,String>> toWork(@RequestBody CommuteVo request) {
		boolean success=mapper.toWork(request.getSabun());
		Map<String,String> response=new HashMap<>();
		
		if(success) {
			response.put("message", "출근이 기록되었습니다.");
			return ResponseEntity.ok(response);
		} else {
			response.put("message", "출근 기록에 실패했습니다.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	
	@PostMapping("/toHome") 
	public @ResponseBody ResponseEntity<Map<String,String>> toHome(@RequestBody CommuteVo request) {
		boolean success=mapper.toHome(request.getSabun());
		Map<String,String> response=new HashMap<>();
		
		if(success) {
			response.put("message", "퇴근이 기록되었습니다.");
			return ResponseEntity.ok(response);
		} else {
			response.put("message", "퇴근 기록이 실패했습니다.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	
	@GetMapping("/sawonList")
	public String sawonList(@RequestParam(defaultValue = "") String depart,
			Model model) {
		ArrayList<SawonVo> slist=mapper.sawonList(depart); 
		model.addAttribute("slist",slist);
		
		String part="전체";
		switch(depart) {
			case "s01":part="총무부"; break;
			case "s02":part="자재부"; break;
			case "s03":part="영업부"; break;
			case "s04":part="감사부"; break;
		}
		
		model.addAttribute("part",part);
		
		return "/sawonList";
		
	}
	
	@PostMapping("/sawonList")
	public @ResponseBody ResponseEntity<Map<String,Object>> addSawon(@RequestBody SawonVo sawon) {
		String depart=sawon.getDepart();
		String sabun=depart+String.format("%03d", mapper.getSabun(depart));
		sawon.setSabun(sabun);
		mapper.addSawon(sawon);
		
		Map<String, Object> response=new HashMap<>();
		response.put("success", true);
		response.put("message", "사원이 성공적으로 추가됐습니다.");
		response.put("sabun", sabun);
		return ResponseEntity.ok(response);
		
	}
	
	@DeleteMapping("/sawonList/{sabun}/{id}")
	public @ResponseBody ResponseEntity<Map<String,Object>> delSawon(@PathVariable String sabun,
			@PathVariable String id) {
		Map<String,Object> response=new HashMap<>();
		
		System.out.println("아이디"+id+"사번"+sabun);
		boolean isDeleted=mapper.delSawon(sabun,id);
		if(isDeleted) {
			response.put("success", true);
			response.put("message", "사원이 성공적으로 삭제 되었습니다.");
			return ResponseEntity.ok(response);
		} else {
			response.put("success", false);
			response.put("message", "사원을 찾을수 없습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
	}
	
	@GetMapping("/sawonInfo/{sabun}/{id}")
	public @ResponseBody SawonVo showSawon(@PathVariable String sabun,
			@PathVariable String id) {
		SawonVo sawon=mapper.showSawon(id);
        sawon.setDepart(sawon.getSabun().substring(0,3));	

		return sawon;		
	}
	
	@PostMapping("/updateSawon")
	public @ResponseBody ResponseEntity<Map<String, Object>> updateSawon(@RequestBody SawonVo sawon) {
		Map<String, Object> response=new HashMap<>();
		if(!sawon.getSabun().substring(0,3).equals(sawon.getDepart())) {
			String sabun=sawon.getDepart()+String.format("%03d", mapper.getSabun(sawon.getDepart())); 
			sawon.setSabun(sabun);			
		}
		int result=mapper.updateSawon(sawon);
		
		if(result>0) {
			response.put("success", true);
			response.put("message", "사원 정보가 수정되었습니다.");
		} else {
			response.put("success", false);
			response.put("message", "업데이트 할 사원 정보를 찾을수 없습니다.");
		}
		
		return ResponseEntity.ok(response);
	}
}
