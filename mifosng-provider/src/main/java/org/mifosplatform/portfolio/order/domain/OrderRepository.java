package org.mifosplatform.portfolio.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository  extends JpaRepository<Order, Long>,
   JpaSpecificationExecutor<Order>{

    @Query("from Order order where order.id=(select max(newOrder.id) from Order newOrder where newOrder.orderNo =:orderNo and newOrder.status=3 )")
	Order findOldOrderByOrderNO(@Param("orderNo")String orderNo);

	
}
