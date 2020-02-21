#include <types.h>
#include <lib.h>
#include <synch.h>
#include <test.h>
#include <thread.h>

#include "investor_producer.h"


/*
 * **********************************************************************
 * YOU ARE FREE TO CHANGE THIS FILE BELOW THIS POINT AS YOU SEE FIT
 *
 */



/*
 * **********************************************************************
 * FUNCTIONS EXECUTED BY CUSTOMER THREADS
 * **********************************************************************
 */


/*
 * order_item()
 *
 * Takes one argument specifying the item produces. The function
 * makes the item order available to producer threads and then blocks until the producers
 * have produced the item with that appropriate for the customers.
 *
 * The item itself contains the number of ordered items.
 */ 

void order_item(void *itm){
	(void)itm; // to avoid warning
  panic("You need to write some code!!!!");
}

/**
 * consume_item() 
 * Customer consume items which were served by the producers.
 * affected variables in the order queue, on item quantity, order type, requested by, served by
 * customer should keep records for his/her spending in shopping
 * and update spending account
 **/
void consume_item(unsigned long customernum){
    (void) customernum; // avoid warning 
    panic("You need to write some code!!!!");
}

/*
 * end_shoping()
 *
 * This function is called by customers when they go home. It could be
 * used to keep track of the number of remaining customers to allow
 * producer threads to exit when no customers remain.
 */

void end_shoping(){
    panic("You need to write some code!!!!");
}


/*
 * **********************************************************************
 * FUNCTIONS EXECUTED BY ITEM PRODUCER THREADS
 * **********************************************************************
 */

/*
 * take_order()
 *
 * This function waits for a new order to be submitted by
 * customers. When submitted, it records the details and returns a
 * pointer to something representing the order.
 *
 * The return pointer type is void * to allow freedom of representation
 * of orders.
 *
 * The function can return NULL to signal the producer thread it can now
 * exit as there are no customers nor orders left.
 */
 
void *take_order(){
  panic("You need to write some code!!!!");
  return NULL; //modify
}


/*
 * produce_item()
 *
 * This function produce an item if the investment is available for the product
 *
 */

void produce_item(void *v){
	(void)v;
    panic("You need to write some code!!!!");
}


/*
 * serve_order()
 *
 * Takes a produced item and makes it available to the waiting customer.
 */

void serve_order(void *v,unsigned long producernumber){
	(void)v;
	(void)producernumber;
    panic("You need to write some code!!!!");
}

/**
 * calculate_loan_amount()
 * Calculate loan amount
 */
long int calculate_loan_amount(void* itm){
	(void)itm;
    panic("You need to write some code!!!!");
    return 0;
}

/**
 * void loan_request()
 * Request for loan from bank
 */
void loan_request(void *amount,unsigned long producernumber){
	(void)amount;
	(void)producernumber;
    panic("You need to write some code!!!!");
}

/**
 * loan_reimburse()
 * Return loan amount and service charge
 */
void loan_reimburse(void * loan,unsigned long producernumber){
	(void)loan;
	(void)producernumber;
    panic("You need to write some code!!!!");
}

/*
 * **********************************************************************
 * INITIALISATION AND CLEANUP FUNCTIONS
 * **********************************************************************
 */


/*
 * initialize()
 *
 * Perform any initialization you need before opening the investor-producer process to
 * producers and customers
 */

void initialize(){
    panic("You need to write some code!!!!");
}

/*
 * finish()
 *
 * Perform any cleanup investor-producer process after the finish everything and everybody
 * has gone home.
 */

void finish(){
    panic("You need to write some code!!!!");
}
