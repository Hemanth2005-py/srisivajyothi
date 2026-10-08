package com.mymart;

import com.mymart.model.Order;
import com.mymart.repository.OrderRepository;
import com.mymart.service.InvoicePdfService;
import com.mymart.service.ReportExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportExportServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReportExportService reportExportService;

    private List<Order> sampleOrders;

    @BeforeEach
    void setUp() {
        sampleOrders = new ArrayList<>();
        sampleOrders.add(Order.builder()
                .id("ord-1")
                .orderNumber("MM-20261008-0001")
                .createdAt(Instant.now())
                .customerSnapshot(new Order.CustomerSnapshot("Rajesh", "rajesh@example.com", "9876543210"))
                .deliveryAddress(new Order.DeliveryAddress("MG Road", "Bengaluru", "Karnataka", "560001", "Mall", "Call before arrival"))
                .items(List.of(new Order.OrderItem("p1", "Milk", "SKU1", "Amul", "1 L", "", 2, 70.0, 65.0, 130.0)))
                .originalTotal(140.0)
                .discountTotal(10.0)
                .deliveryCharge(35.0)
                .taxTotal(6.5)
                .finalTotal(171.5)
                .paymentMethod("CASH_ON_DELIVERY")
                .paymentStatus("PENDING")
                .orderStatus("PENDING")
                .build());
    }

    @Test
    void testExportCsv() {
        when(orderRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(any(), any())).thenReturn(sampleOrders);

        byte[] csv = reportExportService.exportSalesCsv(LocalDate.now().minusDays(7), LocalDate.now());
        assertNotNull(csv);
        assertTrue(csv.length > 0);
        String content = new String(csv);
        assertTrue(content.contains("Order Number"));
        assertTrue(content.contains("MM-20261008-0001"));
    }

    @Test
    void testExportExcel() {
        when(orderRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(any(), any())).thenReturn(sampleOrders);

        byte[] xlsx = reportExportService.exportSalesExcel(LocalDate.now().minusDays(7), LocalDate.now());
        assertNotNull(xlsx);
        assertTrue(xlsx.length > 0);
    }

    @Test
    void testExportPdf() {
        when(orderRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(any(), any())).thenReturn(sampleOrders);

        byte[] pdf = reportExportService.exportSalesPdf(LocalDate.now().minusDays(7), LocalDate.now());
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testInvoicePdfService() {
        InvoicePdfService invoicePdfService = new InvoicePdfService();
        byte[] invoiceBytes = invoicePdfService.generateOrderInvoice(sampleOrders.get(0));
        assertNotNull(invoiceBytes);
        assertTrue(invoiceBytes.length > 0);
    }
}
