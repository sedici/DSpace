// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package ar.edu.unlp.sedici.sedici2003.web;

import ar.edu.unlp.sedici.sedici2003.model.DsiRelTesauro;
import java.io.UnsupportedEncodingException;
import java.lang.Integer;
import java.lang.String;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

privileged aspect DsiRelTesauroController_Roo_Controller {
    
    @RequestMapping(method = RequestMethod.POST)
    public String DsiRelTesauroController.create(@Valid DsiRelTesauro dsiRelTesauro, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("dsiRelTesauro", dsiRelTesauro);
            return "dsireltesauroes/create";
        }
        uiModel.asMap().clear();
        dsiRelTesauro.persist();
        return "redirect:/dsireltesauroes/" + encodeUrlPathSegment(dsiRelTesauro.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String DsiRelTesauroController.createForm(Model uiModel) {
        uiModel.addAttribute("dsiRelTesauro", new DsiRelTesauro());
        return "dsireltesauroes/create";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String DsiRelTesauroController.show(@PathVariable("id") Integer id, Model uiModel) {
        uiModel.addAttribute("dsireltesauro", DsiRelTesauro.findDsiRelTesauro(id));
        uiModel.addAttribute("itemId", id);
        return "dsireltesauroes/show";
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public String DsiRelTesauroController.list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            uiModel.addAttribute("dsireltesauroes", DsiRelTesauro.findDsiRelTesauroEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) DsiRelTesauro.countDsiRelTesauroes() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("dsireltesauroes", DsiRelTesauro.findAllDsiRelTesauroes());
        }
        return "dsireltesauroes/list";
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    public String DsiRelTesauroController.update(@Valid DsiRelTesauro dsiRelTesauro, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("dsiRelTesauro", dsiRelTesauro);
            return "dsireltesauroes/update";
        }
        uiModel.asMap().clear();
        dsiRelTesauro.merge();
        return "redirect:/dsireltesauroes/" + encodeUrlPathSegment(dsiRelTesauro.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String DsiRelTesauroController.updateForm(@PathVariable("id") Integer id, Model uiModel) {
        uiModel.addAttribute("dsiRelTesauro", DsiRelTesauro.findDsiRelTesauro(id));
        return "dsireltesauroes/update";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String DsiRelTesauroController.delete(@PathVariable("id") Integer id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        DsiRelTesauro.findDsiRelTesauro(id).remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/dsireltesauroes";
    }
    
    @ModelAttribute("dsireltesauroes")
    public Collection<DsiRelTesauro> DsiRelTesauroController.populateDsiRelTesauroes() {
        return DsiRelTesauro.findAllDsiRelTesauroes();
    }
    
    String DsiRelTesauroController.encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        }
        catch (UnsupportedEncodingException uee) {}
        return pathSegment;
    }
    
}