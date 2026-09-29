package com.example.demo.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import com.example.demo.repository.IPermissionRepository;
import com.example.demo.repository.IRoleRepository;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private IPermissionRepository permissionRepository;

    @Mock
    private IRoleRepository roleRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private Permission read;
    private Permission create;

    @BeforeEach
    void setUp() {
     
        read = new Permission(1L, "READ_PRODUCT", "Consultar productos");
     
        create = new Permission(2L, "CREATE_PRODUCT", "Crear productos");
    
    }

    @Test
    void findAll_returnsAllPermissions() {

        when(permissionRepository.findAll()).thenReturn(List.of(read, create));

        List<Permission> result = permissionService.findAll();

        assertEquals(2, result.size());

        verify(permissionRepository).findAll();

    }

    @Test
    void findById_existing_returnsPermission() {

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(read));

        assertEquals(read, permissionService.findById(1L));

    }

    @Test
    void findById_notExisting_throwsException() {

        when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> permissionService.findById(99L));

    }

    @Test
    void findByName_existing_returnsPermission() {

        when(permissionRepository.findByName("READ_PRODUCT")).thenReturn(Optional.of(read));

        assertEquals(read, permissionService.findByName("READ_PRODUCT"));

    }

    @Test
    void findByName_notExisting_throwsException() {

        when(permissionRepository.findByName("NOPE")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> permissionService.findByName("NOPE"));

    }

    @Test
    void create_valid_savesPermissionWithNullId() {

        Permission newPermission = new Permission(50L, "DELETE_ORDER", "Eliminar pedidos");

        when(permissionRepository.existsByName("DELETE_ORDER")).thenReturn(false);

        when(permissionRepository.save(any(Permission.class))).thenAnswer(inv -> inv.getArgument(0));

        Permission result = permissionService.create(newPermission);

        assertNull(result.getId());

        assertEquals("DELETE_ORDER", result.getName());

        verify(permissionRepository).save(newPermission);

    }

    @Test
    void create_nullName_throwsException() {
        Permission invalid = new Permission(null, null, "Sin nombre");

        assertThrows(IllegalArgumentException.class, () -> permissionService.create(invalid));

        verify(permissionRepository, never()).save(any());
    }

    @Test
    void create_blankName_throwsException() {

        Permission invalid = new Permission(null, "   ", "Nombre vacio");

        assertThrows(IllegalArgumentException.class, () -> permissionService.create(invalid));

        verify(permissionRepository, never()).save(any());

    }

    @Test
    void create_duplicateName_throwsException() {

        Permission duplicate = new Permission(null, "READ_PRODUCT", "Duplicado");

        when(permissionRepository.existsByName("READ_PRODUCT")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> permissionService.create(duplicate));

        verify(permissionRepository, never()).save(any());

    }

    @Test
    void update_newName_updatesPermission() {

        Permission changes = new Permission(null, "READ_ALL", "Consultar todo");

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(read));

        when(permissionRepository.existsByName("READ_ALL")).thenReturn(false);

        when(permissionRepository.save(any(Permission.class))).thenAnswer(inv -> inv.getArgument(0));

        Permission result = permissionService.update(1L, changes);


        assertEquals(1L, result.getId());

        assertEquals("READ_ALL", result.getName());

        assertEquals("Consultar todo", result.getDescription());

    }

    @Test
    void update_sameName_doesNotCheckDuplicate() {

        Permission changes = new Permission(null, "READ_PRODUCT", "Nueva descripcion");

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(read));

        when(permissionRepository.save(any(Permission.class))).thenAnswer(inv -> inv.getArgument(0));

        Permission result = permissionService.update(1L, changes);

        assertEquals("Nueva descripcion", result.getDescription());

        verify(permissionRepository, never()).existsByName(anyString());

    }

    @Test
    void update_duplicateName_throwsException() {

        Permission changes = new Permission(null, "CREATE_PRODUCT", "Duplicado");

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(read));

        when(permissionRepository.existsByName("CREATE_PRODUCT")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> permissionService.update(1L, changes));

        verify(permissionRepository, never()).save(any());

    }

    @Test
    void update_nullName_throwsException() {

        Permission changes = new Permission(null, null, "Sin nombre");

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(read));

        assertThrows(IllegalArgumentException.class, () -> permissionService.update(1L, changes));

    }

    @Test
    void update_blankName_throwsException() {

        Permission changes = new Permission(null, " ", "Vacio");

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(read));

        assertThrows(IllegalArgumentException.class, () -> permissionService.update(1L, changes));

    }

    @Test
    void update_notExisting_throwsException() {

        when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,() -> permissionService.update(99L, new Permission(null, "X", "X")));
    
    }

    @Test
    void delete_withoutRoles_deletesPermission() {
    
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(read));
    
        when(roleRepository.findByPermissionsId(1L)).thenReturn(List.of());

        permissionService.delete(1L);

        verify(permissionRepository).delete(read);
    
        verify(roleRepository, never()).save(any());
    
    }

    @Test
    void delete_roleWithOtherPermissions_removesFromRoleAndDeletes() {
    
        Role admin = new Role(1L, "ADMIN", "Administrador", new HashSet<>(Set.of(read, create)));
    
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(read));
    
        when(roleRepository.findByPermissionsId(1L)).thenReturn(List.of(admin));

        permissionService.delete(1L);

        assertFalse(admin.getPermissions().contains(read));
    
        assertEquals(1, admin.getPermissions().size());
    
        verify(roleRepository).save(admin);
    
        verify(permissionRepository).delete(read);
    
    }

    @Test
    void delete_roleWouldBeLeftWithoutPermissions_throwsException() {
    
        Role customer = new Role(3L, "CUSTOMER", "Cliente", new HashSet<>(Set.of(read)));
    
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(read));
    
        when(roleRepository.findByPermissionsId(1L)).thenReturn(List.of(customer));

        assertThrows(IllegalStateException.class, () -> permissionService.delete(1L));
    
        verify(roleRepository, never()).save(any());
    
        verify(permissionRepository, never()).delete(any());
    
    }

    @Test
    void delete_notExisting_throwsException() {
    
        when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

    
        assertThrows(NoSuchElementException.class, () -> permissionService.delete(99L));
    
    }

}