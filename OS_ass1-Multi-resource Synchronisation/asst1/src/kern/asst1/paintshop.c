#include <types.h>
#include <lib.h>
#include <synch.h>
#include <test.h>
#include <thread.h>

#include "paintshop.h"
#include "paintshop_driver.h"



/*
 * **********************************************************************
 * YOU ARE FREE TO CHANGE THIS FILE BELOW THIS POINT AS YOU SEE FIT
 *
 */

#define buffer_size NCUSTOMERS

int remaining_customers;

// buffers for orders and ready paintcans
void *ready_buffer[buffer_size];
struct paintcan *order_buffer[buffer_size];

//binary semaphores for access control
struct semaphore *orderbuffer_mutex;
struct semaphore *readybuffer_mutex;
struct semaphore *customer_no_mutex;
struct semaphore *tints_mutex;

//counting semaphores for controlling access to full or empty buffer
struct semaphore *orderbuffer_full;
struct semaphore *orderbuffer_empty;
struct semaphore *readybuffer_full;
struct semaphore *readybuffer_empty;

/*
 * **********************************************************************
 * FUNCTIONS EXECUTED BY CUSTOMER THREADS
 * **********************************************************************
 */

/*
 * order_paint()
 *
 * Takes one argument specifying the can to be filled. The function
 * makes the can available to staff threads and then blocks until the staff
 * have filled the can with the appropriately tinted paint.
 *
 * The can itself contains an array of requested tints.
 */ 

void order_paint(struct paintcan *can)
{
  //reduce one slot from order buffer
  P(orderbuffer_empty);
  //access the buffer
  P(orderbuffer_mutex);

  //put the can in empty slot of order buffer
  int i = 0;
  for(i = 0; i < buffer_size; i++)
  {
    if(order_buffer[i] == NULL)
    {
      order_buffer[i] = can;
      break;
    }
  }

  V(orderbuffer_mutex);
  //done accessing array

  V(orderbuffer_full);

  bool order_found = false;
  // wait for order to become ready
  while(1)
  {
    // decrement number of cans that can be taken
    P(readybuffer_full);
    // access ready buffer
    P(readybuffer_mutex);

    //check if ordered can in ready buffer
    for(i = 0; i < buffer_size; i++)
    {
      if((struct paintcan *)ready_buffer[i] == can)
      {
        ready_buffer[i] = NULL;
        order_found = true;
        break;
      }
    }
    V(readybuffer_mutex);
    // done accesing buffer
    V(readybuffer_full);

    if(order_found)
    {
      //reduce empty slots in ready buffer
      V(readybuffer_empty);
      //break if found or keep waiting in the loop
      break;
    }

  }

}



/*
 * go_home()
 *
 * This function is called by customers when they go home. It could be
 * used to keep track of the number of remaining customers to allow
 * paint shop staff threads to exit when no customers remain.
 */

void go_home()
{
  //access number of remaining customer
  P(customer_no_mutex);
  remaining_customers--;
  V(customer_no_mutex);
}


/*
 * **********************************************************************
 * FUNCTIONS EXECUTED BY PAINT SHOP STAFF THREADS
 * **********************************************************************
 */

/*
 * take_order()
 *
 * This function waits for a new order to be submitted by
 * customers. When submitted, it records the details, and returns a
 * pointer to something representing the order.
 *
 * The return pointer type is void * to allow freedom of representation
 * of orders.
 *
 * The function can return NULL to signal the staff thread it can now
 * exit as their are no customers nor orders left. 
 */
 
void * take_order()
{
  void *can;

  bool order_found=false;
  while(1) {
    //no customer left -> stuff departs
    if(remaining_customers == 0)
    {
      return NULL;
    }

    // decrement the number of ordered cans
    P(orderbuffer_full);
    // access buffer exclusively
    P(orderbuffer_mutex);

    // look for a requests in order buffer
    int i = 0;
    for(i = 0; i < buffer_size; i++)
    {
      if(order_buffer[i]!=NULL)
        {
          can= (void *)order_buffer[i];
          order_buffer[i] = NULL;
          order_found = true;
          //found a order, stop search
          break;
        }
     }
      V(orderbuffer_mutex);
      // release exclusive access

      V(orderbuffer_full);

      if(order_found)
      {
        // more slot in order buffer since one was taken
        V(orderbuffer_empty);
        break;
      }
    }

  return can;
}


/*
 * fill_order()
 *
 * This function takes an order generated by take order and fills the
 * order using the mix() function to tint the paint.
 *
 * NOTE: IT NEEDS TO ENSURE THAT MIX HAS EXCLUSIVE ACCESS TO THE TINTS
 * IT NEEDS TO USE TO FILE THE ORDER.
 */

void fill_order(void *v)
{
  // lock access to tint
    P(tints_mutex);
    mix(v);
    V(tints_mutex);
    // release exclusive access
}


/*
 * serve_order()
 *
 * Takes a filled order and makes it available to the waiting customer.
 */

void serve_order(void *v)
{
  P(readybuffer_empty);

  P(readybuffer_mutex);

  // put the can in an empty space in ready buffer
  int i = 0;
  for(i = 0; i < buffer_size; i++)
  {
    if(ready_buffer[i] == NULL)
    {
      ready_buffer[i] = v;
      break;
    }
  }
  V(readybuffer_mutex);

  // indicate increase in readybuffer
  V(readybuffer_full);
}



/*
 * **********************************************************************
 * INITIALISATION AND CLEANUP FUNCTIONS
 * **********************************************************************
 */


/*
 * paintshop_open()
 *
 * Perform any initialisation you need prior to opening the paint shop to
 * staff and customers
 */

void paintshop_open()
{

  remaining_customers = NCUSTOMERS;

  /* binary semaphores for accessing critical region */
  orderbuffer_mutex = sem_create("orderbuffer_mutex", 1);
  readybuffer_mutex = sem_create("readybuffer_mutex", 1);

  tints_mutex = sem_create("tints_mutex", 1);
  customer_no_mutex = sem_create("customer_no_mutex", 1);

  /* counting semaphores for controlling access based on buffer slots */
  readybuffer_full = sem_create("readybuffer_full", 0);
  orderbuffer_full = sem_create("orderbuffer_full", 0);

  readybuffer_empty = sem_create("readybuffer_empty", buffer_size);
  orderbuffer_empty = sem_create("orderbuffer_empty", buffer_size);

  // initialize buffers as empty
  int i = 0;
  for(i = 0; i < buffer_size; i++)
  {
    order_buffer[i]=NULL;
    ready_buffer[i] = NULL;
  }
}

/*
 * paintshop_close()
 *
 * Perform any cleanup after the paint shop has closed and everybody
 * has gone home.
 */

void paintshop_close()
{
  // cleanup the semaphores
  sem_destroy(orderbuffer_mutex);
  sem_destroy(readybuffer_mutex);

  sem_destroy(readybuffer_full);
  sem_destroy(readybuffer_empty);

  sem_destroy(orderbuffer_full);
  sem_destroy(orderbuffer_empty);

  sem_destroy(tints_mutex);
  sem_destroy(customer_no_mutex);
}
