package moe.ofs.backend.system.controllers;

import cn.hutool.core.lang.tree.Tree;
import moe.ofs.backend.domain.admin.frontend.NavMenu;
import moe.ofs.backend.system.services.NavMenuManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("system/nav_menu")
public class NavMenuManageController {

    private final NavMenuManageService service;

    public NavMenuManageController(NavMenuManageService service) {
        this.service = service;
    }

    @GetMapping("list")
    public Set<NavMenu> getNavMenus() {
        return service.findAllNavMenu();
    }

    @GetMapping("tree")
    public List<Tree<Long>> getNavMenusTree() {
        return service.findAllNavMenuAsTree();
    }

    @PostMapping("add")
    public int addNavMenu(@RequestBody NavMenu menu) {
        System.out.println("menu = " + menu);
        return service.addNavMenu(menu);
    }

    @PostMapping("delete")
    public int deleteNavMenu(@RequestBody NavMenu menu) {
        return service.deleteNavMenu(menu);
    }
}