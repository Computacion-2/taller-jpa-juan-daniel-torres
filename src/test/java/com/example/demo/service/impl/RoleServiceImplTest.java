package com.example.demo.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.demo.model.Permission;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.IPermissionRepository;
import com.example.demo.repository.IRoleRepository;
import com.example.demo.repository.IUserRepository;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private IRoleRepository roleRepository;

    @Mock
    private IPermissionRepository permissionRepository;

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Permission read;
   
    private Permission create;
   
    private Role admin;

    @BeforeEach
    void setUp() {
   
        read = new Permission(1L, "READ_PRODUCT", "Consultar productos");
   
        create = new Permission(2L, "CREATE_PRODUCT", "Crear productos");
   
        admin = new Role(1L, "ADMIN", "Administrador", new HashSet<>(Set.of(read, create)));
   
    }

    private User userWithRoles(Long id, String username, Role... roles) {
   
        return new User(id, username, username + "@tienda.com", "123", "Nombre",new HashSet<>(Set.of(roles)), new ArrayList<>());
    }

    private Set<Permission> permissionIds(Long... ids) {
      
        Set<Permission> permissions = new HashSet<>();
      
        for (Long id : ids) {
      
            permissions.add(new Permission(id, null, null));
      
        }
      
        return permissions;
    }

    @Test
    void findAll_returnsAllRoles() {
      
        when(roleRepository.findAll()).thenReturn(List.of(admin));

        assertEquals(1, roleService.findAll().size());
      
        verify(roleRepository).findAll();
    
    }

    @Test
    void findById_existing_returnsRole() {
    
        when(roleRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertEquals(admin, roleService.findById(1L));
    
    }

    @Test
    void findById_notExisting_throwsException() {

        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> roleService.findById(99L));

    }

    @Test
    void findByName_existing_returnsRole() {
     
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(admin));

        assertEquals(admin, roleService.findByName("ADMIN"));
    
    }

    @Test
    void findByName_notExisting_throwsException() {
      
        when(roleRepository.findByName("NOPE")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> roleService.findByName("NOPE"));
    
    }

    @Test
    void create_valid_savesRoleWithResolvedPermissions() {
    
        Role newRole = new Role(99L, "AUDITOR", "Auditor", permissionIds(1L));
    
        when(roleRepository.existsByName("AUDITOR")).thenReturn(false);
    
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(read));
    
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        Role result = roleService.create(newRole);

        assertNull(result.getId());

        assertEquals(1, result.getPermissions().size());

        assertTrue(result.getPermissions().contains(read));

        verify(roleRepository).save(newRole);

    }

    @Test
    void create_nullName_throwsException() {

        Role invalid = new Role(null, null, "Sin nombre", permissionIds(1L));

        assertThrows(IllegalArgumentException.class, () -> roleService.create(invalid));

        verify(roleRepository, never()).save(any());

    }

    @Test
    void create_duplicateName_throwsException() {

        Role duplicate = new Role(null, "ADMIN", "Duplicado", permissionIds(1L));

        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> roleService.create(duplicate));

        verify(roleRepository, never()).save(any());

    }

    @Test
    void create_nullPermissions_throwsException() {

        Role invalid = new Role(null, "AUDITOR", "Sin permisos", null);

        when(roleRepository.existsByName("AUDITOR")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> roleService.create(invalid));

        verify(roleRepository, never()).save(any());

    }

    @Test
    void create_emptyPermissions_throwsException() {

        Role invalid = new Role(null, "AUDITOR", "Sin permisos", new HashSet<>());

        when(roleRepository.existsByName("AUDITOR")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> roleService.create(invalid));

        verify(roleRepository, never()).save(any());

    }

    @Test
    void create_permissionNotExisting_throwsException() {

        Role invalid = new Role(null, "AUDITOR", "Permiso falso", permissionIds(77L));

        when(roleRepository.existsByName("AUDITOR")).thenReturn(false);

        when(permissionRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> roleService.create(invalid));

        verify(roleRepository, never()).save(any());

    }

    @Test
    void update_newName_updatesRole() {

        Role changes = new Role(null, "SUPER_ADMIN", "Nuevo", permissionIds(1L));

        when(roleRepository.findById(1L)).thenReturn(Optional.of(admin));

        when(roleRepository.existsByName("SUPER_ADMIN")).thenReturn(false);

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(read));

        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        Role result = roleService.update(1L, changes);

        assertEquals(1L, result.getId());

        assertEquals("SUPER_ADMIN", result.getName());

        assertEquals("Nuevo", result.getDescription());

        assertEquals(1, result.getPermissions().size());

    }

    @Test
    void update_sameName_doesNotCheckDuplicate() {

        Role changes = new Role(null, "ADMIN", "Misma", permissionIds(1L, 2L));

        when(roleRepository.findById(1L)).thenReturn(Optional.of(admin));

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(read));

        when(permissionRepository.findById(2L)).thenReturn(Optional.of(create));

        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        roleService.update(1L, changes);

        verify(roleRepository, never()).existsByName(anyString());

    }

    @Test
    void update_duplicateName_throwsException() {

        Role changes = new Role(null, "SELLER", "Duplicado", permissionIds(1L));

        when(roleRepository.findById(1L)).thenReturn(Optional.of(admin));

        when(roleRepository.existsByName("SELLER")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> roleService.update(1L, changes));

        verify(roleRepository, never()).save(any());

    }

    @Test
    void update_blankName_throwsException() {

        Role changes = new Role(null, "  ", "Vacio", permissionIds(1L));

        when(roleRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(IllegalArgumentException.class, () -> roleService.update(1L, changes));

    }

    @Test
    void update_emptyPermissions_throwsException() {

        Role changes = new Role(null, "ADMIN", "Sin permisos", new HashSet<>());

        when(roleRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(IllegalArgumentException.class, () -> roleService.update(1L, changes));

        verify(roleRepository, never()).save(any());

    }

    @Test
    void update_notExisting_throwsException() {

        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,() -> roleService.update(99L, new Role(null, "X", "X", permissionIds(1L))));
    }

    @Test
    void delete_withoutUsers_deletesRole() {

        when(roleRepository.findById(1L)).thenReturn(Optional.of(admin));

        when(userRepository.findByRolesId(1L)).thenReturn(List.of());

        roleService.delete(1L);


        verify(roleRepository).delete(admin);

        verify(userRepository, never()).save(any());

    }

    @Test
    void delete_userWithOtherRoles_removesRoleAndDeletes() {

        Role seller = new Role(2L, "SELLER", "Vendedor", new HashSet<>(Set.of(read)));

        User user = userWithRoles(5L, "mixto1", admin, seller);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(admin));

        when(userRepository.findByRolesId(1L)).thenReturn(List.of(user));

        roleService.delete(1L);

        assertFalse(user.getRoles().contains(admin));

        assertEquals(1, user.getRoles().size());

        verify(userRepository).save(user);

        verify(roleRepository).delete(admin);

    }

    @Test
    void delete_userWouldBeLeftWithoutRoles_throwsException() {

        User user = userWithRoles(1L, "admin", admin);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(admin));

        when(userRepository.findByRolesId(1L)).thenReturn(List.of(user));

        assertThrows(IllegalStateException.class, () -> roleService.delete(1L));

        verify(userRepository, never()).save(any());

        verify(roleRepository, never()).delete(any());

    }

    @Test
    void delete_notExisting_throwsException() {

        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> roleService.delete(99L));

    }

    @Test
    void addPermission_valid_addsPermission() {

        Role customer = new Role(3L, "CUSTOMER", "Cliente", new HashSet<>(Set.of(read)));

        when(roleRepository.findById(3L)).thenReturn(Optional.of(customer));

        when(permissionRepository.findById(2L)).thenReturn(Optional.of(create));

        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        Role result = roleService.addPermission(3L, 2L);

        assertEquals(2, result.getPermissions().size());

        assertTrue(result.getPermissions().contains(create));

    }

    @Test
    void addPermission_permissionNotExisting_throwsException() {

        when(roleRepository.findById(1L)).thenReturn(Optional.of(admin));

        when(permissionRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> roleService.addPermission(1L, 77L));

        verify(roleRepository, never()).save(any());

    }

    @Test
    void removePermission_valid_removesPermission() {

        when(roleRepository.findById(1L)).thenReturn(Optional.of(admin));

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(read));

        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        Role result = roleService.removePermission(1L, 1L);

        assertEquals(1, result.getPermissions().size());

        assertFalse(result.getPermissions().contains(read));

    }

    @Test
    void removePermission_roleDoesNotHavePermission_throwsException() {

        Role customer = new Role(3L, "CUSTOMER", "Cliente", new HashSet<>(Set.of(read)));

        when(roleRepository.findById(3L)).thenReturn(Optional.of(customer));

        when(permissionRepository.findById(2L)).thenReturn(Optional.of(create));

        assertThrows(IllegalArgumentException.class, () -> roleService.removePermission(3L, 2L));

        verify(roleRepository, never()).save(any());

    }

    @Test
    void removePermission_lastPermission_throwsException() {

        Role customer = new Role(3L, "CUSTOMER", "Cliente", new HashSet<>(Set.of(read)));

        when(roleRepository.findById(3L)).thenReturn(Optional.of(customer));

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(read));

        assertThrows(IllegalStateException.class, () -> roleService.removePermission(3L, 1L));

        verify(roleRepository, never()).save(any());

    }

}