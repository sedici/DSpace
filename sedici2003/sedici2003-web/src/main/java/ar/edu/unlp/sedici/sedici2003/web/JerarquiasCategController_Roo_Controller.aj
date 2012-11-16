// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package ar.edu.unlp.sedici.sedici2003.web;

import ar.edu.unlp.sedici.sedici2003.model.JerarquiasCateg;
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

privileged aspect JerarquiasCategController_Roo_Controller {
    
    @RequestMapping(method = RequestMethod.POST)
    public String JerarquiasCategController.create(@Valid JerarquiasCateg jerarquiasCateg, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("jerarquiasCateg", jerarquiasCateg);
            return "jerarquiascategs/create";
        }
        uiModel.asMap().clear();
        jerarquiasCateg.persist();
        return "redirect:/jerarquiascategs/" + encodeUrlPathSegment(jerarquiasCateg.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String JerarquiasCategController.createForm(Model uiModel) {
        uiModel.addAttribute("jerarquiasCateg", new JerarquiasCateg());
        return "jerarquiascategs/create";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String JerarquiasCategController.show(@PathVariable("id") Integer id, Model uiModel) {
        uiModel.addAttribute("jerarquiascateg", JerarquiasCateg.findJerarquiasCateg(id));
        uiModel.addAttribute("itemId", id);
        return "jerarquiascategs/show";
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public String JerarquiasCategController.list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            uiModel.addAttribute("jerarquiascategs", JerarquiasCateg.findJerarquiasCategEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) JerarquiasCateg.countJerarquiasCategs() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("jerarquiascategs", JerarquiasCateg.findAllJerarquiasCategs());
        }
        return "jerarquiascategs/list";
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    public String JerarquiasCategController.update(@Valid JerarquiasCateg jerarquiasCateg, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("jerarquiasCateg", jerarquiasCateg);
            return "jerarquiascategs/update";
        }
        uiModel.asMap().clear();
        jerarquiasCateg.merge();
        return "redirect:/jerarquiascategs/" + encodeUrlPathSegment(jerarquiasCateg.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String JerarquiasCategController.updateForm(@PathVariable("id") Integer id, Model uiModel) {
        uiModel.addAttribute("jerarquiasCateg", JerarquiasCateg.findJerarquiasCateg(id));
        return "jerarquiascategs/update";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String JerarquiasCategController.delete(@PathVariable("id") Integer id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        JerarquiasCateg.findJerarquiasCateg(id).remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/jerarquiascategs";
    }
    
    @ModelAttribute("jerarquiascategs")
    public Collection<JerarquiasCateg> JerarquiasCategController.populateJerarquiasCategs() {
        return JerarquiasCateg.findAllJerarquiasCategs();
    }
    
    String JerarquiasCategController.encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
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