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
import com.example.demo.model.Order;
import com.example.demo.model.Permission;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.IOrderRepository;
import com.example.demo.repository.IRoleRepository;
import com.example.demo.repository.IUserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IRoleRepository roleRepository;

    @Mock
    private IOrderRepository orderRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private Role seller;
    
    private Role customer;
    
    private User client;

    @BeforeEach
    void setUp() {
    
        Permission read = new Permission(4L, "READ_PRODUCT", "Consultar productos");
    
        seller = new Role(2L, "SELLER", "Vendedor", new HashSet<>(Set.of(read)));
    
        customer = new Role(3L, "CUSTOMER", "Cliente", new HashSet<>(Set.of(read)));
    
        client = new User(3L, "cliente1", "cliente1@tienda.com", "cli123", "Carlos Perez",new HashSet<>(Set.of(customer)), new ArrayList<>());
    
    }

    private Set<Role> roleIds(Long... ids) {
    
        Set<Role> roles = new HashSet<>();
    
        for (Long id : ids) {
    
            roles.add(new Role(id, null, null, new HashSet<>()));
    
        }
    
        return roles;
    
    }

    private User userData(String username, String email, String password, Set<Role> roles) {
    
        return new User(null, username, email, password, "Nombre", roles, new ArrayList<>());
    
    }

    @Test
    void findAll_returnsAllUsers() {
    
        when(userRepository.findAll()).thenReturn(List.of(client));

        assertEquals(1, userService.findAll().size());
    
        verify(userRepository).findAll();
    
    }

    @Test
    void findById_existing_returnsUser() {
    
        when(userRepository.findById(3L)).thenReturn(Optional.of(client));

        assertEquals(client, userService.findById(3L));
    
    }

    @Test
    void findById_notExisting_throwsException() {
    
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.findById(99L));
    
    }

    @Test
    void findByUsername_existing_returnsUser() {
    
        when(userRepository.findByUsername("cliente1")).thenReturn(Optional.of(client));

        assertEquals(client, userService.findByUsername("cliente1"));
    
    }

    @Test
    void findByUsername_notExisting_throwsException() {
    
        when(userRepository.findByUsername("nadie")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.findByUsername("nadie"));
    
    }

    @Test
    void create_valid_savesUserWithResolvedRoles() {
    
        User newUser = new User(50L, "nuevo", "nuevo@tienda.com", "pw", "Nuevo",roleIds(3L), new ArrayList<>());
        
        when(userRepository.existsByUsername("nuevo")).thenReturn(false);
        
        when(userRepository.existsByEmail("nuevo@tienda.com")).thenReturn(false);
        
        when(roleRepository.findById(3L)).thenReturn(Optional.of(customer));
        
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.create(newUser);

        assertNull(result.getId());
        
        assertEquals(1, result.getRoles().size());
        
        assertTrue(result.getRoles().contains(customer));
        
        verify(userRepository).save(newUser);
    
    }

    @Test
    void create_nullUsername_throwsException() {
    
        User invalid = userData(null, "a@tienda.com", "pw", roleIds(3L));

        assertThrows(IllegalArgumentException.class, () -> userService.create(invalid));
    
        verify(userRepository, never()).save(any());
    
    }

    @Test
    void create_blankEmail_throwsException() {
    
        User invalid = userData("nuevo", "  ", "pw", roleIds(3L));

        assertThrows(IllegalArgumentException.class, () -> userService.create(invalid));
    
        verify(userRepository, never()).save(any());
    
    }

    @Test
    void create_nullPassword_throwsException() {
    
        User invalid = userData("nuevo", "nuevo@tienda.com", null, roleIds(3L));

        assertThrows(IllegalArgumentException.class, () -> userService.create(invalid));
    
        verify(userRepository, never()).save(any());
    
    }

    @Test
    void create_duplicateUsername_throwsException() {
    
        User duplicate = userData("cliente1", "otro@tienda.com", "pw", roleIds(3L));
    
        when(userRepository.existsByUsername("cliente1")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.create(duplicate));
    
        verify(userRepository, never()).save(any());
    
    }

    @Test
    void create_duplicateEmail_throwsException() {
    
        User duplicate = userData("nuevo", "cliente1@tienda.com", "pw", roleIds(3L));
    
        when(userRepository.existsByUsername("nuevo")).thenReturn(false);
    
        when(userRepository.existsByEmail("cliente1@tienda.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.create(duplicate));
    
        verify(userRepository, never()).save(any());
    
    }

    @Test
    void create_nullRoles_throwsException() {
    
        User invalid = userData("nuevo", "nuevo@tienda.com", "pw", null);
    
        when(userRepository.existsByUsername("nuevo")).thenReturn(false);
    
        when(userRepository.existsByEmail("nuevo@tienda.com")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.create(invalid));
    
        verify(userRepository, never()).save(any());
    
    }

    @Test
    void create_emptyRoles_throwsException() {
    
        User invalid = userData("nuevo", "nuevo@tienda.com", "pw", new HashSet<>());
    
        when(userRepository.existsByUsername("nuevo")).thenReturn(false);
    
        when(userRepository.existsByEmail("nuevo@tienda.com")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.create(invalid));
    
        verify(userRepository, never()).save(any());
    
    }

    @Test
    void create_roleNotExisting_throwsException() {
    
        User invalid = userData("nuevo", "nuevo@tienda.com", "pw", roleIds(77L));
    
        when(userRepository.existsByUsername("nuevo")).thenReturn(false);
    
        when(userRepository.existsByEmail("nuevo@tienda.com")).thenReturn(false);
    
        when(roleRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.create(invalid));
    
        verify(userRepository, never()).save(any());
    
    }

    @Test
    void update_newUsernameEmailAndPassword_updatesUser() {

        User changes = userData("carlos", "carlos@tienda.com", "nueva", roleIds(2L, 3L));

        when(userRepository.findById(3L)).thenReturn(Optional.of(client));

        when(userRepository.existsByUsername("carlos")).thenReturn(false);

        when(userRepository.existsByEmail("carlos@tienda.com")).thenReturn(false);

        when(roleRepository.findById(2L)).thenReturn(Optional.of(seller));

        when(roleRepository.findById(3L)).thenReturn(Optional.of(customer));

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.update(3L, changes);

        assertEquals(3L, result.getId());

        assertEquals("carlos", result.getUsername());

        assertEquals("carlos@tienda.com", result.getEmail());

        assertEquals("nueva", result.getPassword());

        assertEquals(2, result.getRoles().size());

    }

    @Test
    void update_sameUsernameAndEmail_nullPassword_keepsPassword() {

        User changes = userData("cliente1", "cliente1@tienda.com", null, roleIds(3L));

        when(userRepository.findById(3L)).thenReturn(Optional.of(client));

        when(roleRepository.findById(3L)).thenReturn(Optional.of(customer));

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.update(3L, changes);

        assertEquals("cli123", result.getPassword());

        verify(userRepository, never()).existsByUsername(anyString());

        verify(userRepository, never()).existsByEmail(anyString());

    }

    @Test
    void update_blankPassword_keepsPassword() {

        User changes = userData("cliente1", "cliente1@tienda.com", "   ", roleIds(3L));

        when(userRepository.findById(3L)).thenReturn(Optional.of(client));

        when(roleRepository.findById(3L)).thenReturn(Optional.of(customer));

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.update(3L, changes);

        assertEquals("cli123", result.getPassword());

    }

    @Test
    void update_duplicateUsername_throwsException() {

        User changes = userData("admin", "cliente1@tienda.com", null, roleIds(3L));

        when(userRepository.findById(3L)).thenReturn(Optional.of(client));

        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.update(3L, changes));

        verify(userRepository, never()).save(any());

    }

    @Test
    void update_duplicateEmail_throwsException() {

        User changes = userData("cliente1", "admin@tienda.com", null, roleIds(3L));

        when(userRepository.findById(3L)).thenReturn(Optional.of(client));

        when(userRepository.existsByEmail("admin@tienda.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.update(3L, changes));

        verify(userRepository, never()).save(any());

    }

    @Test
    void update_blankUsername_throwsException() {

        User changes = userData(" ", "cliente1@tienda.com", null, roleIds(3L));

        when(userRepository.findById(3L)).thenReturn(Optional.of(client));

        assertThrows(IllegalArgumentException.class, () -> userService.update(3L, changes));

    }

    @Test
    void update_emptyRoles_throwsException() {

        User changes = userData("cliente1", "cliente1@tienda.com", null, new HashSet<>());
        when(userRepository.findById(3L)).thenReturn(Optional.of(client));

        assertThrows(IllegalArgumentException.class, () -> userService.update(3L, changes));

        verify(userRepository, never()).save(any());

    }

    @Test
    void update_notExisting_throwsException() {

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,() -> userService.update(99L, userData("x", "x@x.com", "x", roleIds(3L))));
   
    }

    @Test
    void delete_withoutOrders_deletesUser() {

        when(userRepository.findById(3L)).thenReturn(Optional.of(client));

        when(orderRepository.findByUserId(3L)).thenReturn(List.of());

        userService.delete(3L);

        verify(userRepository).delete(client);

    }

    @Test
    void delete_withOrders_throwsException() {

        when(userRepository.findById(3L)).thenReturn(Optional.of(client));

        when(orderRepository.findByUserId(3L)).thenReturn(List.of(new Order()));

        assertThrows(IllegalStateException.class, () -> userService.delete(3L));

        verify(userRepository, never()).delete(any());

    }

    @Test
    void delete_notExisting_throwsException() {

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.delete(99L));

    }

    @Test
    void addRole_valid_addsRole() {

        when(userRepository.findById(3L)).thenReturn(Optional.of(client));

        when(roleRepository.findById(2L)).thenReturn(Optional.of(seller));

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.addRole(3L, 2L);

        assertEquals(2, result.getRoles().size());

        assertTrue(result.getRoles().contains(seller));

    }

    @Test
    void addRole_roleNotExisting_throwsException() {

        when(userRepository.findById(3L)).thenReturn(Optional.of(client));

        when(roleRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.addRole(3L, 77L));

        verify(userRepository, never()).save(any());

    }

    @Test
    void removeRole_valid_removesRole() {

        client.getRoles().add(seller);

        when(userRepository.findById(3L)).thenReturn(Optional.of(client));

        when(roleRepository.findById(2L)).thenReturn(Optional.of(seller));

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.removeRole(3L, 2L);

        assertEquals(1, result.getRoles().size());

        assertFalse(result.getRoles().contains(seller));

    }

    @Test
    void removeRole_userDoesNotHaveRole_throwsException() {

        when(userRepository.findById(3L)).thenReturn(Optional.of(client));

        when(roleRepository.findById(2L)).thenReturn(Optional.of(seller));

        assertThrows(IllegalArgumentException.class, () -> userService.removeRole(3L, 2L));

        verify(userRepository, never()).save(any());

    }

    @Test
    void removeRole_lastRole_throwsException() {

        when(userRepository.findById(3L)).thenReturn(Optional.of(client));

        when(roleRepository.findById(3L)).thenReturn(Optional.of(customer));

        assertThrows(IllegalStateException.class, () -> userService.removeRole(3L, 3L));

        verify(userRepository, never()).save(any());

    }

}