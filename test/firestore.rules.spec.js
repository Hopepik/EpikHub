const fs = require('fs');
const assert = require('assert');
const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds
} = require('@firebase/rules-unit-testing');

const PROJECT_ID = 'epik-hub2-reactivation-test';
let testEnv;

describe('Epik Hub Firestore Security Rules Final Test Suite', () => {
  before(async () => {
    testEnv = await initializeTestEnvironment({
      projectId: PROJECT_ID,
      firestore: {
        rules: fs.readFileSync('firestore.rules', 'utf8'),
        host: '127.0.0.1',
        port: 8080
      }
    });
  });

  after(async () => {
    await testEnv.cleanup();
  });

  beforeEach(async () => {
    await testEnv.clearFirestore();

    // Seed base test database state via admin context (bypassing security rules)
    await testEnv.withSecurityRulesDisabled(async (context) => {
      const db = context.firestore();

      // Users
      await db.doc('users/customer_1').set({
        uid: 'customer_1',
        fullName: 'John Customer',
        email: 'john@example.com',
        phone: '+265888000111',
        role: 'customer',
        companyId: null,
        createdAt: 1700000000000,
        updatedAt: 1700000000000
      });

      await db.doc('users/customer_2').set({
        uid: 'customer_2',
        fullName: 'Jane Customer',
        email: 'jane@example.com',
        phone: '+265888000222',
        role: 'customer',
        companyId: null,
        createdAt: 1700000000000,
        updatedAt: 1700000000000
      });

      await db.doc('users/admin_1').set({
        uid: 'admin_1',
        fullName: 'Epik Admin',
        email: 'epikcomputech@gmail.com',
        phone: '+265880551373',
        role: 'company_admin',
        companyId: 'epik_compu_tech',
        createdAt: 1700000000000,
        updatedAt: 1700000000000
      });

      await db.doc('users/admin_2').set({
        uid: 'admin_2',
        fullName: 'Other Admin',
        email: 'other@example.com',
        phone: '+265999000222',
        role: 'company_admin',
        companyId: 'other_company',
        createdAt: 1700000000000,
        updatedAt: 1700000000000
      });

      await db.doc('users/admin_inactive_co').set({
        uid: 'admin_inactive_co',
        fullName: 'Inactive Co Admin',
        email: 'inactiveadmin@example.com',
        phone: '+265111222333',
        role: 'company_admin',
        companyId: 'inactive_company',
        createdAt: 1700000000000,
        updatedAt: 1700000000000
      });

      await db.doc('users/admin_nonexistent_co').set({
        uid: 'admin_nonexistent_co',
        fullName: 'Nonexistent Co Admin',
        email: 'nonexistentadmin@example.com',
        phone: '+265111222444',
        role: 'company_admin',
        companyId: 'nonexistent_company',
        createdAt: 1700000000000,
        updatedAt: 1700000000000
      });

      await db.doc('users/malformed_admin').set({
        uid: 'malformed_admin',
        fullName: 'Malformed Admin',
        email: 'bad@example.com',
        phone: '+265111222333',
        role: 'company_admin',
        companyId: null
      });

      // Companies
      await db.doc('companies/epik_compu_tech').set({
        id: 'epik_compu_tech',
        name: 'Epik Compu-Tech',
        description: 'Tech services',
        phone: '+265880551373',
        email: 'epikcomputech@gmail.com',
        location: 'Blantyre',
        logoUrl: 'https://example.com/logo.png',
        status: 'active',
        createdAt: 1700000000000,
        updatedAt: 1700000000000
      });

      await db.doc('companies/inactive_company').set({
        id: 'inactive_company',
        name: 'Inactive Co',
        description: 'Closed business',
        phone: '+265123456789',
        email: 'inactive@example.com',
        location: 'Lilongwe',
        logoUrl: null,
        status: 'inactive',
        createdAt: 1700000000000,
        updatedAt: 1700000000000
      });

      // Services
      await db.doc('services/service_active').set({
        id: 'service_active',
        companyId: 'epik_compu_tech',
        name: 'Active Repair',
        description: 'Computer repair service',
        category: 'Maintenance',
        price: 30000,
        imageUrl: 'https://example.com/repair.png',
        isActive: true,
        companyActive: true,
        createdAt: 1700000000000,
        updatedAt: 1700000000000
      });

      await db.doc('services/service_stale_active').set({
        id: 'service_stale_active',
        companyId: 'inactive_company', // Company is INACTIVE!
        name: 'Stale Active Service',
        description: 'Stale active flag',
        category: 'General',
        price: 10000,
        imageUrl: null,
        isActive: true,
        companyActive: true, // Stale companyActive flag!
        createdAt: 1700000000000,
        updatedAt: 1700000000000
      });

      await db.doc('services/service_inactive').set({
        id: 'service_inactive',
        companyId: 'epik_compu_tech',
        name: 'Inactive Service',
        description: 'Old service',
        category: 'Maintenance',
        price: 5000,
        imageUrl: null,
        isActive: false,
        companyActive: true,
        createdAt: 1700000000000,
        updatedAt: 1700000000000
      });

      await db.doc('services/service_inactive_company').set({
        id: 'service_inactive_company',
        companyId: 'inactive_company',
        name: 'Service In Inactive Co',
        description: 'Service',
        category: 'Maintenance',
        price: 5000,
        imageUrl: null,
        isActive: false,
        companyActive: true,
        createdAt: 1700000000000,
        updatedAt: 1700000000000
      });

      await db.doc('services/service_nonexistent_company').set({
        id: 'service_nonexistent_company',
        companyId: 'nonexistent_company',
        name: 'Service In Nonexistent Co',
        description: 'Service',
        category: 'Maintenance',
        price: 5000,
        imageUrl: null,
        isActive: false,
        companyActive: true,
        createdAt: 1700000000000,
        updatedAt: 1700000000000
      });

      await db.doc('services/service_other').set({
        id: 'service_other',
        companyId: 'other_company',
        name: 'Other Company Service',
        description: 'Other service',
        category: 'General',
        price: 20000,
        imageUrl: null,
        isActive: true,
        companyActive: true,
        createdAt: 1700000000000,
        updatedAt: 1700000000000
      });

      // Service Requests
      await db.doc('serviceRequests/req_pending').set({
        id: 'req_pending',
        customerId: 'customer_1',
        companyId: 'epik_compu_tech',
        serviceId: 'service_active',
        serviceName: 'Active Repair',
        servicePrice: 30000,
        customerName: 'John Customer',
        customerPhone: '+265888000111',
        description: 'Please fix my screen',
        status: 'PENDING',
        createdAt: 1700000000000,
        updatedAt: 1700000000000
      });

      await db.doc('serviceRequests/req_accepted').set({
        id: 'req_accepted',
        customerId: 'customer_1',
        companyId: 'epik_compu_tech',
        serviceId: 'service_active',
        serviceName: 'Active Repair',
        servicePrice: 30000,
        customerName: 'John Customer',
        customerPhone: '+265888000111',
        description: 'Please fix my screen',
        status: 'ACCEPTED',
        createdAt: 1700000000000,
        updatedAt: 1700000000000
      });

      await db.doc('serviceRequests/req_in_progress').set({
        id: 'req_in_progress',
        customerId: 'customer_1',
        companyId: 'epik_compu_tech',
        serviceId: 'service_active',
        serviceName: 'Active Repair',
        servicePrice: 30000,
        customerName: 'John Customer',
        customerPhone: '+265888000111',
        description: 'Please fix my screen',
        status: 'IN_PROGRESS',
        createdAt: 1700000000000,
        updatedAt: 1700000000000
      });

      await db.doc('serviceRequests/req_completed').set({
        id: 'req_completed',
        customerId: 'customer_1',
        companyId: 'epik_compu_tech',
        serviceId: 'service_active',
        serviceName: 'Active Repair',
        servicePrice: 30000,
        customerName: 'John Customer',
        customerPhone: '+265888000111',
        description: 'Please fix my screen',
        status: 'COMPLETED',
        createdAt: 1700000000000,
        updatedAt: 1700000000000
      });
    });
  });

  // 1. COMPANY & SERVICE DEACTIVATION READ/WRITE ENFORCEMENT
  it('allows guest to read active service belonging to active company', async () => {
    const unauth = testEnv.unauthenticatedContext();
    await assertSucceeds(unauth.firestore().doc('services/service_active').get());
  });

  it('denies guest from reading service belonging to inactive company even if companyActive flag is stale true', async () => {
    const unauth = testEnv.unauthenticatedContext();
    await assertFails(unauth.firestore().doc('services/service_stale_active').get());
  });

  it('denies guest from reading inactive service of active company', async () => {
    const unauth = testEnv.unauthenticatedContext();
    await assertFails(unauth.firestore().doc('services/service_inactive').get());
  });

  it('denies admin of inactive company from creating a service', async () => {
    const admin = testEnv.authenticatedContext('admin_inactive_co');
    await assertFails(admin.firestore().doc('services/service_new_inactive').set({
      id: 'service_new_inactive',
      companyId: 'inactive_company',
      name: 'Service for Inactive Co',
      description: 'Desc',
      category: 'Software',
      price: 10000,
      isActive: true,
      companyActive: true,
      createdAt: 1700000000000,
      updatedAt: 1700000000000
    }));
  });

  // 2. USER PROFILE PRIVACY & STRICT SCHEMA
  it('allows user to read their own profile', async () => {
    const cust = testEnv.authenticatedContext('customer_1');
    await assertSucceeds(cust.firestore().doc('users/customer_1').get());
  });

  it('denies company_admin from reading an unrelated customer profile in users collection', async () => {
    const admin = testEnv.authenticatedContext('admin_1');
    await assertFails(admin.firestore().doc('users/customer_1').get());
  });

  it('denies customer_2 from reading customer_1 profile', async () => {
    const cust2 = testEnv.authenticatedContext('customer_2');
    await assertFails(cust2.firestore().doc('users/customer_1').get());
  });

  it('allows customer to create their profile with role customer and valid integer timestamp schema', async () => {
    const cust = testEnv.authenticatedContext('customer_new');
    await assertSucceeds(cust.firestore().doc('users/customer_new').set({
      uid: 'customer_new',
      fullName: 'New Customer',
      email: 'new@example.com',
      phone: '+265888999000',
      role: 'customer',
      createdAt: 1700000000000,
      updatedAt: 1700000000000
    }));
  });

  it('denies user creation with floating-point timestamp', async () => {
    const cust = testEnv.authenticatedContext('customer_float');
    await assertFails(cust.firestore().doc('users/customer_float').set({
      uid: 'customer_float',
      fullName: 'Float Timestamp User',
      email: 'float@example.com',
      phone: '+265888999000',
      role: 'customer',
      createdAt: 1700000000000.5, // Invalid floating-point timestamp!
      updatedAt: 1700000000000
    }));
  });

  it('denies user creation with zero or negative timestamp', async () => {
    const cust = testEnv.authenticatedContext('customer_neg');
    await assertFails(cust.firestore().doc('users/customer_neg').set({
      uid: 'customer_neg',
      fullName: 'Negative Timestamp User',
      email: 'neg@example.com',
      phone: '+265888999000',
      role: 'customer',
      createdAt: 0, // Invalid non-positive timestamp!
      updatedAt: 1700000000000
    }));
  });

  it('denies user creation with unexpected extra fields', async () => {
    const cust = testEnv.authenticatedContext('customer_extra');
    await assertFails(cust.firestore().doc('users/customer_extra').set({
      uid: 'customer_extra',
      fullName: 'Extra Customer',
      email: 'extra@example.com',
      phone: '+265888999000',
      role: 'customer',
      unexpectedField: 'hack', // Unexpected extra key!
      createdAt: 1700000000000,
      updatedAt: 1700000000000
    }));
  });

  it('denies user from registering as company_admin', async () => {
    const cust = testEnv.authenticatedContext('unauth_user');
    await assertFails(cust.firestore().doc('users/unauth_user').set({
      uid: 'unauth_user',
      fullName: 'Hacker',
      email: 'hacker@example.com',
      phone: '+265000000000',
      role: 'company_admin',
      companyId: 'epik_compu_tech',
      createdAt: 1700000000000,
      updatedAt: 1700000000000
    }));
  });

  it('denies user from modifying role or companyId during profile update', async () => {
    const cust = testEnv.authenticatedContext('customer_1');
    await assertFails(cust.firestore().doc('users/customer_1').update({
      role: 'company_admin',
      companyId: 'epik_compu_tech',
      updatedAt: 1700000001000
    }));
  });

  // 3. COMPANY DOCUMENT PROTECTION & DOCUMENT ID MATCH
  it('allows guest to read active company', async () => {
    const unauth = testEnv.unauthenticatedContext();
    await assertSucceeds(unauth.firestore().doc('companies/epik_compu_tech').get());
  });

  it('denies guest from reading inactive company', async () => {
    const unauth = testEnv.unauthenticatedContext();
    await assertFails(unauth.firestore().doc('companies/inactive_company').get());
  });

  it('allows company admin to update allowed profile fields of their own company', async () => {
    const admin = testEnv.authenticatedContext('admin_1');
    await assertSucceeds(admin.firestore().doc('companies/epik_compu_tech').update({
      name: 'Epik Compu-Tech Updated',
      description: 'Updated description',
      phone: '+265880551373',
      email: 'epikcomputech@gmail.com',
      location: 'Zingwangwa, Blantyre',
      updatedAt: 1700000001000
    }));
  });

  it('denies company admin from modifying protected company fields (status, id, createdAt)', async () => {
    const admin = testEnv.authenticatedContext('admin_1');
    await assertFails(admin.firestore().doc('companies/epik_compu_tech').update({
      status: 'inactive',
      updatedAt: 1700000001000
    }));
  });

  // 4. SERVICE MANAGEMENT, DEACTIVATION, REACTIVATION & DELETION PROHIBITION
  it('allows company admin to create service for their active company with valid schema', async () => {
    const admin = testEnv.authenticatedContext('admin_1');
    await assertSucceeds(admin.firestore().doc('services/service_new').set({
      id: 'service_new',
      companyId: 'epik_compu_tech',
      name: 'New Custom Service',
      description: 'Custom description',
      category: 'Software',
      price: 10000,
      isActive: true,
      companyActive: true,
      createdAt: 1700000000000,
      updatedAt: 1700000000000
    }));
  });

  it('1. Allows an administrator to deactivate a service belonging to their company', async () => {
    const admin = testEnv.authenticatedContext('admin_1');
    await assertSucceeds(admin.firestore().doc('services/service_active').update({
      isActive: false,
      updatedAt: 1700000001000
    }));
  });

  it('2. Allows an administrator to reactivate a service when their company is active', async () => {
    const admin = testEnv.authenticatedContext('admin_1');
    await assertSucceeds(admin.firestore().doc('services/service_inactive').update({
      isActive: true,
      updatedAt: 1700000001000
    }));
  });

  it('3. Denies an administrator from reactivating a service when their company is inactive', async () => {
    const admin = testEnv.authenticatedContext('admin_inactive_co');
    await assertFails(admin.firestore().doc('services/service_inactive_company').update({
      isActive: true,
      updatedAt: 1700000001000
    }));
  });

  it('4. Denies reactivation when the referenced company document does not exist', async () => {
    const admin = testEnv.authenticatedContext('admin_nonexistent_co');
    await assertFails(admin.firestore().doc('services/service_nonexistent_company').update({
      isActive: true,
      updatedAt: 1700000001000
    }));
  });

  it('5. Denies an administrator from updating another company\'s service', async () => {
    const admin1 = testEnv.authenticatedContext('admin_1');
    await assertFails(admin1.firestore().doc('services/service_other').update({
      isActive: false,
      updatedAt: 1700000001000
    }));
  });

  it('denies permanent service deletion by company admin (allow delete: if false)', async () => {
    const admin = testEnv.authenticatedContext('admin_1');
    await assertFails(admin.firestore().doc('services/service_active').delete());
  });

  it('denies service creation with document ID mismatch', async () => {
    const admin = testEnv.authenticatedContext('admin_1');
    await assertFails(admin.firestore().doc('services/service_doc_123').set({
      id: 'service_doc_999', // Mismatched internal ID!
      companyId: 'epik_compu_tech',
      name: 'Mismatched Service',
      description: 'Desc',
      category: 'Gen',
      price: 1000,
      isActive: true,
      companyActive: true,
      createdAt: 1700000000000,
      updatedAt: 1700000000000
    }));
  });

  it('denies service creation with negative price', async () => {
    const admin = testEnv.authenticatedContext('admin_1');
    await assertFails(admin.firestore().doc('services/service_negative').set({
      id: 'service_negative',
      companyId: 'epik_compu_tech',
      name: 'Negative Service',
      description: 'Desc',
      category: 'Gen',
      price: -500, // Invalid negative price!
      isActive: true,
      companyActive: true,
      createdAt: 1700000000000,
      updatedAt: 1700000000000
    }));
  });

  it('denies company admin from creating service for another company', async () => {
    const admin = testEnv.authenticatedContext('admin_1');
    await assertFails(admin.firestore().doc('services/service_other_new').set({
      id: 'service_other_new',
      companyId: 'other_company',
      name: 'Unauthorized Service',
      description: 'Custom description',
      category: 'Software',
      price: 10000,
      isActive: true,
      companyActive: true,
      createdAt: 1700000000000,
      updatedAt: 1700000000000
    }));
  });

  it('denies company admin from modifying protected service fields (companyId, companyActive, createdAt)', async () => {
    const admin = testEnv.authenticatedContext('admin_1');
    await assertFails(admin.firestore().doc('services/service_active').update({
      companyId: 'other_company',
      updatedAt: 1700000001000
    }));
  });

  // 5. SERVICE REQUEST CREATION, VALIDATION & RELATIONSHIPS
  it('allows customer_1 to read their own request', async () => {
    const cust1 = testEnv.authenticatedContext('customer_1');
    await assertSucceeds(cust1.firestore().doc('serviceRequests/req_pending').get());
  });

  it('denies customer_2 from reading customer_1 service request', async () => {
    const cust2 = testEnv.authenticatedContext('customer_2');
    await assertFails(cust2.firestore().doc('serviceRequests/req_pending').get());
  });

  it('allows company admin_1 to read requests submitted to their company', async () => {
    const admin1 = testEnv.authenticatedContext('admin_1');
    await assertSucceeds(admin1.firestore().doc('serviceRequests/req_pending').get());
  });

  it('denies company admin_2 from reading admin_1 company service requests', async () => {
    const admin2 = testEnv.authenticatedContext('admin_2');
    await assertFails(admin2.firestore().doc('serviceRequests/req_pending').get());
  });

  it('allows customer to create request referencing active service and active company with valid data', async () => {
    const cust = testEnv.authenticatedContext('customer_1');
    await assertSucceeds(cust.firestore().doc('serviceRequests/req_new').set({
      id: 'req_new',
      customerId: 'customer_1',
      companyId: 'epik_compu_tech',
      serviceId: 'service_active',
      serviceName: 'Active Repair',
      servicePrice: 30000,
      customerName: 'John Customer',
      customerPhone: '+265888000111',
      description: 'Please fix my laptop charger',
      status: 'PENDING',
      createdAt: 1700000000000,
      updatedAt: 1700000000000
    }));
  });

  it('denies customer from creating request with document ID mismatch', async () => {
    const cust = testEnv.authenticatedContext('customer_1');
    await assertFails(cust.firestore().doc('serviceRequests/req_doc_123').set({
      id: 'req_doc_999', // Mismatched internal ID!
      customerId: 'customer_1',
      companyId: 'epik_compu_tech',
      serviceId: 'service_active',
      serviceName: 'Active Repair',
      servicePrice: 30000,
      customerName: 'John Customer',
      customerPhone: '+265888000111',
      description: 'Please fix my laptop charger',
      status: 'PENDING',
      createdAt: 1700000000000,
      updatedAt: 1700000000000
    }));
  });

  it('denies customer from creating request with empty customerPhone or description', async () => {
    const cust = testEnv.authenticatedContext('customer_1');
    await assertFails(cust.firestore().doc('serviceRequests/req_empty').set({
      id: 'req_empty',
      customerId: 'customer_1',
      companyId: 'epik_compu_tech',
      serviceId: 'service_active',
      serviceName: 'Active Repair',
      servicePrice: 30000,
      customerName: 'John Customer',
      customerPhone: '', // Empty phone string!
      description: 'Please fix',
      status: 'PENDING',
      createdAt: 1700000000000,
      updatedAt: 1700000000000
    }));
  });

  it('denies customer from creating request for an inactive service', async () => {
    const cust = testEnv.authenticatedContext('customer_1');
    await assertFails(cust.firestore().doc('serviceRequests/req_bad').set({
      id: 'req_bad',
      customerId: 'customer_1',
      companyId: 'epik_compu_tech',
      serviceId: 'service_inactive',
      serviceName: 'Inactive Service',
      servicePrice: 5000,
      customerName: 'John Customer',
      customerPhone: '+265888000111',
      description: 'Please do this service',
      status: 'PENDING',
      createdAt: 1700000000000,
      updatedAt: 1700000000000
    }));
  });

  it('denies customer from creating request for a service belonging to an inactive company', async () => {
    const cust = testEnv.authenticatedContext('customer_1');
    await assertFails(cust.firestore().doc('serviceRequests/req_bad_comp').set({
      id: 'req_bad_comp',
      customerId: 'customer_1',
      companyId: 'inactive_company',
      serviceId: 'service_stale_active',
      serviceName: 'Stale Active Service',
      servicePrice: 10000,
      customerName: 'John Customer',
      customerPhone: '+265888000111',
      description: 'Please do this service',
      status: 'PENDING',
      createdAt: 1700000000000,
      updatedAt: 1700000000000
    }));
  });

  it('denies customer from creating request for a nonexistent service', async () => {
    const cust = testEnv.authenticatedContext('customer_1');
    await assertFails(cust.firestore().doc('serviceRequests/req_bad').set({
      id: 'req_bad',
      customerId: 'customer_1',
      companyId: 'epik_compu_tech',
      serviceId: 'nonexistent_service',
      serviceName: 'Nonexistent Service',
      servicePrice: 10000,
      customerName: 'John Customer',
      customerPhone: '+265888000111',
      description: 'Fix this',
      status: 'PENDING',
      createdAt: 1700000000000,
      updatedAt: 1700000000000
    }));
  });

  it('denies customer from creating request referencing service belonging to another company', async () => {
    const cust = testEnv.authenticatedContext('customer_1');
    await assertFails(cust.firestore().doc('serviceRequests/req_bad').set({
      id: 'req_bad',
      customerId: 'customer_1',
      companyId: 'epik_compu_tech', // Mismatched companyId!
      serviceId: 'service_other',   // Belongs to 'other_company'
      serviceName: 'Other Company Service',
      servicePrice: 20000,
      customerName: 'John Customer',
      customerPhone: '+265888000111',
      description: 'Fix this',
      status: 'PENDING',
      createdAt: 1700000000000,
      updatedAt: 1700000000000
    }));
  });

  it('denies customer from manipulating service price or name on creation', async () => {
    const cust = testEnv.authenticatedContext('customer_1');
    await assertFails(cust.firestore().doc('serviceRequests/req_bad').set({
      id: 'req_bad',
      customerId: 'customer_1',
      companyId: 'epik_compu_tech',
      serviceId: 'service_active',
      serviceName: 'Active Repair',
      servicePrice: 1, // Manipulated price! Real price is 30000
      customerName: 'John Customer',
      customerPhone: '+265888000111',
      description: 'Fix this',
      status: 'PENDING',
      createdAt: 1700000000000,
      updatedAt: 1700000000000
    }));
  });

  // 6. SERVICE REQUEST UPDATE & STATE MACHINE
  it('allows customer to cancel their own PENDING request', async () => {
    const cust = testEnv.authenticatedContext('customer_1');
    await assertSucceeds(cust.firestore().doc('serviceRequests/req_pending').update({
      status: 'CANCELLED',
      updatedAt: 1700000001000
    }));
  });

  it('denies customer from modifying protected request fields (description, price) while cancelling', async () => {
    const cust = testEnv.authenticatedContext('customer_1');
    await assertFails(cust.firestore().doc('serviceRequests/req_pending').update({
      status: 'CANCELLED',
      description: 'Hacked description',
      updatedAt: 1700000001000
    }));
  });

  it('allows admin to accept PENDING request', async () => {
    const admin = testEnv.authenticatedContext('admin_1');
    await assertSucceeds(admin.firestore().doc('serviceRequests/req_pending').update({
      status: 'ACCEPTED',
      updatedAt: 1700000001000
    }));
  });

  it('denies admin from modifying protected request fields (customerName, description) while updating status', async () => {
    const admin = testEnv.authenticatedContext('admin_1');
    await assertFails(admin.firestore().doc('serviceRequests/req_pending').update({
      status: 'ACCEPTED',
      customerName: 'Hacked Customer Name',
      updatedAt: 1700000001000
    }));
  });

  it('allows admin to move ACCEPTED request to IN_PROGRESS', async () => {
    const admin = testEnv.authenticatedContext('admin_1');
    await assertSucceeds(admin.firestore().doc('serviceRequests/req_accepted').update({
      status: 'IN_PROGRESS',
      updatedAt: 1700000001000
    }));
  });

  it('allows admin to move IN_PROGRESS request to COMPLETED', async () => {
    const admin = testEnv.authenticatedContext('admin_1');
    await assertSucceeds(admin.firestore().doc('serviceRequests/req_in_progress').update({
      status: 'COMPLETED',
      updatedAt: 1700000001000
    }));
  });

  it('denies admin from moving PENDING request directly to COMPLETED', async () => {
    const admin = testEnv.authenticatedContext('admin_1');
    await assertFails(admin.firestore().doc('serviceRequests/req_pending').update({
      status: 'COMPLETED',
      updatedAt: 1700000001000
    }));
  });

  it('denies updates to terminal COMPLETED requests', async () => {
    const admin = testEnv.authenticatedContext('admin_1');
    await assertFails(admin.firestore().doc('serviceRequests/req_completed').update({
      status: 'PENDING',
      updatedAt: 1700000001000
    }));
  });

  // 7. MALFORMED PROFILES & UNPROVISIONED ACCESS
  it('denies unprovisioned or malformed admin profiles from performing admin actions', async () => {
    const malformed = testEnv.authenticatedContext('malformed_admin');
    await assertFails(malformed.firestore().doc('services/service_new').set({
      id: 'service_new',
      companyId: 'epik_compu_tech',
      name: 'Service by malformed user',
      description: 'Desc',
      category: 'Gen',
      price: 100,
      isActive: true,
      companyActive: true,
      createdAt: 1700000000000,
      updatedAt: 1700000000000
    }));
  });
});
