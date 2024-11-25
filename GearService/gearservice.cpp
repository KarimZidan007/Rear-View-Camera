#include <iostream>
#include <unistd.h>
#include <sys/system_properties.h>
#include <cstdlib>
#include "include/gpiohal.h"


void gpio_init(GpioHal gpioHal)
{
    gpioHal.exportGpio(4);  
    gpioHal.setGpioDirection(4, "in");        
} 


int main() {
   int gpioVal = 0;
   int previousVal;
   GpioHal gpioHal = GpioHal();  
   gpio_init(gpioHal);
    while (true) {
            previousVal=gpioVal;
            gpioHal.getGpioValue(4, &gpioVal);
            if(previousVal!=gpioVal)
            {
                std::string gpioValStr = std::to_string(gpioVal);
                int ret = __system_property_set("reverseGear", gpioValStr.c_str());
                    if (ret != 0) {
                         std::cout<<("Failed to set system property 'reverseGear'");
                    } 
            }
    }

    return 0;
}
