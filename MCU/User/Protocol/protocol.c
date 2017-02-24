#include "protocol.h"

extern int												SN;
extern uint8_t 										get_one_package;
extern pro_commonCmd							m_pro_commonCmd;
extern pro_errorCmd								m_pro_errorCmd;
extern m2w_returnMcuInfo					m_m2w_returnMcuInfo;
extern w2m_controlMcu							m_w2m_controlMcu;
extern m2w_mcuStatus							m_m2w_mcuStatus;
extern m2w_mcuStatus							m_m2w_mcuStatus_reported;
extern w2m_reportModuleStatus			m_w2m_reportModuleStatus;
extern uint8_t										check_status_time;
extern uint8_t										report_status_idle_time;	
extern uint8_t 										wait_ack_time;
extern uint8_t 										uart_buf[256];
extern uint16_t										uart_Count;
extern uint32_t										wait_wifi_status;
extern int iol_temp;
/*******************************************************************************
* Function Name  : exchangeBytes
* Description    : Ä£ÄâµÄhtons »òÕß ntohs£¬Èç¹ûÏµÍ³Ö§×Ö½ÚĞò¸ü¸Ä¿ÉÖ±½ÓÌæ»»³ÉÏµÍ³º¯Êı
* Input          : value
* Output         : None
* Return         : ¸ü¸Ä¹ı×Ö½ÚĞòµÄshortÊıÖµ
* Attention		   : None
*******************************************************************************/
short	exchangeBytes(short	value)
{
	short			tmp_value;
	uint8_t		*index_1, *index_2;
	
	index_1 = (uint8_t *)&tmp_value;
	index_2 = (uint8_t *)&value;
	
	*index_1 = *(index_2+1);
	*(index_1+1) = *index_2;
	
	return tmp_value;
}

/*******************************************************************************
* Function Name  : SendCommonCmd
* Description    : ·¢ËÍÍ¨ÓÃÃüÁî£¬ÃüÁî×ÖºÍsn×÷Îª²ÎÊı´«Èë£¬¸´ÓÃÍ¨ÓÃÃüÁîÖ¡£¬Ğ´´®¿Ú
* Input          : cmd£ºÒª·¢ËÍµÄÍ¨ÓÃÃüÁîÃüÁî×Ö£¬ sn£ºĞòÁĞºÅ
* Output         : None
* Return         : None
* Attention		   : None
*******************************************************************************/
void	SendCommonCmd(uint8_t cmd, uint8_t sn)
{
	memset(&m_pro_commonCmd, 0, sizeof(pro_commonCmd));
	
	m_pro_commonCmd.head_part.head[0] = 0xFF;
	m_pro_commonCmd.head_part.head[1] = 0xFF;
	m_pro_commonCmd.head_part.len = exchangeBytes(5);
	m_pro_commonCmd.head_part.cmd = cmd;
	m_pro_commonCmd.head_part.sn = sn;
	m_pro_commonCmd.sum = CheckSum((uint8_t *)&m_pro_commonCmd, sizeof(pro_commonCmd));
	
	SendToUart((uint8_t *)&m_pro_commonCmd, sizeof(pro_commonCmd), 0);		
}

/*******************************************************************************
* Function Name  : SendErrorCmd
* Description    : ·¢ËÍ´íÎóÃüÁîÖ¡£¬´íÎóÂëºÍsn×÷Îª²ÎÊı´«Èë£¬¸÷ÖÖ´íÎóÂëÊÊÓÃ
* Input          : error_no:´íÎóÂë£» sn:ĞòÁĞºÅ
* Output         : None
* Return         : None
* Attention		   : None
*******************************************************************************/
void	SendErrorCmd(uint8_t error_no, uint8_t sn)
{
	m_pro_errorCmd.head_part.sn = sn;
	m_pro_errorCmd.error = error_no;
	m_pro_errorCmd.sum = CheckSum((uint8_t *)&m_pro_errorCmd, sizeof(pro_errorCmd));
	
	SendToUart((uint8_t *)&m_pro_errorCmd, sizeof(pro_errorCmd), 0);		
}

/*******************************************************************************
* Function Name  : CmdGetMcuInfo
* Description    : ·µ»ØmcuĞÅÏ¢£¬½ö¸üĞÂsn¼´¿É£¬ÆäËûĞÅÏ¢¹Ì»¯
* Input          : Sn£ºĞòºÅ
* Output         : None
* Return         : None
* Attention		   : None
*******************************************************************************/
void	CmdGetMcuInfo(uint8_t sn)
{
	m_m2w_returnMcuInfo.head_part.sn = sn;
	m_m2w_returnMcuInfo.sum = CheckSum((uint8_t *)&m_m2w_returnMcuInfo, sizeof(m2w_returnMcuInfo));
	
	SendToUart((uint8_t *)&m_m2w_returnMcuInfo, sizeof(m2w_returnMcuInfo), 0);			
}

/*******************************************************************************
* Function Name  : CmdSendMcuP0
* Description    : mcu½ÓÊÕµ½wifiµÄ¿ØÖÆÃüÁî£¬´Ë²¿·ÖÊÇĞèÒªmcu¿ª·¢ÕßÖØµãÊµÏÖµÄ
* Input          : buf£º´®¿Ú½ÓÊÕ»º³åÇøµØÖ·
* Output         : None
* Return         : None
* Attention		   : None
*******************************************************************************/
void	CmdSendMcuP0(uint8_t *buf)
{
	uint8_t		tmp_cmd_buf;
	
	if(buf == NULL) return ;
	
	memcpy(&m_w2m_controlMcu, buf, sizeof(w2m_controlMcu));
	//m_w2m_controlMcu.status_w.motor_speed = exchangeBytes(m_w2m_controlMcu.status_w.motor_speed);
	
	//ÉÏ±¨×´Ì¬
	if(m_w2m_controlMcu.sub_cmd == SUB_CMD_REQUIRE_STATUS) ReportStatus(REQUEST_STATUS);
	
	//¿ØÖÆÃüÁî£¬²Ù×÷×Ö¶ÎË³ĞòÒÀ´ÎÊÇ£º R_on/off, multi Color, R, G, B, motor
	if(m_w2m_controlMcu.sub_cmd == SUB_CMD_CONTROL_MCU){
		//ÏÈ»Ø¸´È·ÈÏ£¬±íÊ¾ÊÕµ½ºÏ·¨µÄ¿ØÖÆÃüÁîÁË£
		SendCommonCmd(CMD_SEND_MCU_P0_ACK, m_w2m_controlMcu.head_part.sn);
		
		//¿ØÖÆÃüÁî±êÖ¾°´ÕÕĞ­Òé±íÃ÷ÄÄ¸ö²Ù×÷×Ö¶ÎÓĞĞ§£¨¶ÔÓ¦µÄÎ»Îª1£©want to control LED R 
		if((m_w2m_controlMcu.cmd_tag & 0x01) == 0x01)
		{
			//0 bit, 1: R on, 0: R off;
			if((m_w2m_controlMcu.status_w.cmd_byte ) == 1)
			{
        //°Ö°Ö»ØÀ´ÁË ºìÂÌÉ«
				LED_RGB_Control(50, 50, 0);
				m_m2w_mcuStatus.status_w.cmd_byte = m_w2m_controlMcu.status_w.cmd_byte;
			}
			else if( (m_w2m_controlMcu.status_w.cmd_byte ) == 2)
			{
        //ÂèÂè»ØÀ´ÁË  ºìÀ¶É«
				LED_RGB_Control(50, 0, 50);
				m_m2w_mcuStatus.status_w.cmd_byte = m_w2m_controlMcu.status_w.cmd_byte;
			}
      else if( (m_w2m_controlMcu.status_w.cmd_byte) == 3)
			{
        //º¢×Ó»ØÀ´ÁË  À¶ÂÌÉ«
				LED_RGB_Control(0, 50, 50);
				m_m2w_mcuStatus.status_w.cmd_byte = m_w2m_controlMcu.status_w.cmd_byte;
			}
      else
      {
				LED_RGB_Control(0, 0, 200);
				m_m2w_mcuStatus.status_w.cmd_byte = m_w2m_controlMcu.status_w.cmd_byte;
      }
		}
      m_m2w_mcuStatus.status_r.soil_humidity=iol_temp;    
      ReportStatus(REPORT_STATUS);
	}
}

/*******************************************************************************
* Function Name  : CmdReportModuleStatus
* Description    : mcuÊÕµ½À´×ÔÓÚwifiÄ£¿éµÄ×´Ì¬±ä»¯Í¨Öª£¬´Ë²¿·ÖÊÇmcu¿ª·¢ÕßÖØµãÊµÏÖµÄ£¬±ÈÈçÔÚÉè±¸µÄÃæ°åÉÏÏÔÊ¾wifiµÄµ±Ç°×´Ì¬
* Input          : buf:´®¿Ú½ÓÊÕ»º³åÇøµØÖ·
* Output         : None
* Return         : None
* Attention		   : None
*******************************************************************************/
void	CmdReportModuleStatus(uint8_t *buf)
{
	if(buf == NULL)	return ;
	
	memcpy(&m_w2m_reportModuleStatus, buf, sizeof(w2m_reportModuleStatus));
	
	//0 bit
	if((m_w2m_reportModuleStatus.status[1] & 0x01) == 0x01)
	{
		//open softap
	}
	else
	{
		//close softap
	}
	
	//1 bit
	if((m_w2m_reportModuleStatus.status[1] & 0x02) == 0x02)
	{
		//open station
	}
	else
	{
		//close station
	}
	
	//2 bit
	if((m_w2m_reportModuleStatus.status[1] & 0x04) == 0x04)
	{
		//open onboarding
	}
	else
	{
		//close onboarding
	}
	
	//3 bit
	if((m_w2m_reportModuleStatus.status[1] & 0x08) == 0x08)
	{
		//open binding
	}
	else
	{
		//close binding
	}
	
	//4 bit
	if((m_w2m_reportModuleStatus.status[1] & 0x10) == 0x10)
	{
		//connect router success
		if(wait_wifi_status == 1){
			wait_wifi_status = 0;
			LED_RGB_Control(0, 0, 0);
		}
	}
	else
	{
		//disconnect from router
	}
	
	//5 bit
	if((m_w2m_reportModuleStatus.status[1] & 0x20) == 0x20)
	{
		//connect M2M success
		if(wait_wifi_status == 1){
			wait_wifi_status = 0;
			LED_RGB_Control(0, 0, 0);
		}
	}
	else
	{
		//disconnect from M2M
	}
	
	SendCommonCmd(CMD_REPORT_MODULE_STATUS_ACK, m_w2m_reportModuleStatus.head_part.sn);
}
	
/*******************************************************************************
* Function Name  : MessageHandle
* Description    : ´®¿ÚÓĞÊı¾İ·¢ÉúÁË£¬ÏÈ¼ì²éÊı¾İÊÇ·ñºÏ·¨£¬ÔÙ½âÎöÊı¾İÖ¡£¬×öÏàÓ¦´¦Àí
* Input          : None
* Output         : None
* Return         : None
* Attention		   : None
*******************************************************************************/
void MessageHandle(void)
{
	pro_headPart	tmp_headPart;		
	memset(&tmp_headPart, 0, sizeof(pro_headPart));
	
	if(get_one_package)
	{			
		get_one_package = 0;
		memcpy(&tmp_headPart, uart_buf, sizeof(pro_headPart));
		
		//Ğ£ÑéÂë´íÎó£¬·µ»Ø´íÎóÃüÁî
		if(CheckSum(uart_buf, uart_Count) != uart_buf[uart_Count-1]) 
		{
			SendErrorCmd(ERROR_CHECKSUM, tmp_headPart.sn);
			return ;
		}
		
		switch(tmp_headPart.cmd)
		{
			//»ñÈ¡mcu infi£¬Í¨ÓÃĞ­Òé£¬mcu¿ª·¢Õß½ö¸ü¸Ä±ØÒªµÄĞÅÏ¢£¨±ÈÈçproduct key£©¼´¿É
			case	CMD_GET_MCU_INFO:
				CmdGetMcuInfo(tmp_headPart.sn);					
				break;	
			
			//ĞÄÌø£¬mcu¿ª·¢Õß¸´ÓÃ¼´¿É£¬²»Ğè¸Ä±ä
			case CMD_SEND_HEARTBEAT:
				SendCommonCmd(CMD_SEND_HEARTBEAT_ACK, tmp_headPart.sn);
				break;
			
			//ÖØÆô£¬mcu¿ª·¢Õß¸´ÓÃ¼´¿É£¬¸ü¸Ä³ÉÏµÍ³µÄÖØÆôº¯Êı¼´¿É£¬±ØĞëµÈ´ı600ºÁÃëÔÙÖØÆô
			case CMD_REBOOT_MCU:
				SendCommonCmd(CMD_REBOOT_MCU_ACK, tmp_headPart.sn);
				delay_ms(600);
				NVIC_SystemReset();
			
			//¿ØÖÆÃüÁî£¬mcu¿ª·¢ÕßÌ×ÓÃÄ£°å£¬ÖØµãÊµÏÖ½âÎö³öÀ´µÄÃüÁîÕıÈ·²Ù×÷ÍâÉè
			case	CMD_SEND_MCU_P0:
				CmdSendMcuP0(uart_buf);
				break;
			
			//wifi×´Ì¬±ä»¯Í¨Öª£¬mcu¿ª·¢ÕßÌ×ÓÃÄ£°å£¬ÖØµãÊµÏÖ²Ù×÷ÍâÉè
			case	CMD_REPORT_MODULE_STATUS:
				CmdReportModuleStatus(uart_buf);
				break;
			
			//·¢ËÍ´íÎóÃüÁî×Ö
			default:
				SendErrorCmd(ERROR_CMD, tmp_headPart.sn);
				break;
		}	
	}
}

/*******************************************************************************
* Function Name  : SendToUart
* Description    : Ïò´®¿Ú·¢ËÍÊı¾İÖ¡
* Input          : buf:Êı¾İÆğÊ¼µØÖ·£» packLen:Êı¾İ³¤¶È£» tag=0,²»µÈ´ıACK£»tag=1,µÈ´ıACK£»
* Output         : None
* Return         : None
* Attention		   : ÈôµÈ´ıACK£¬°´ÕÕĞ­ÒéÊ§°ÜÖØ·¢3´Î£»Êı¾İÇø³öÏÖFF£¬ÔÚÆäºóÔö¼Ó55
*******************************************************************************/
void SendToUart(uint8_t *buf, uint16_t packLen, uint8_t tag)
{
	uint16_t 				i;
	int							Send_num;
	pro_headPart		send_headPart;	
	pro_commonCmd		recv_commonCmd;
	uint8_t					m_55;
	
	m_55 = 0x55;
	for(i=0;i<packLen;i++){
		UART1_Send_DATA(buf[i]);
		if(i >=2 && buf[i] == 0xFF) UART1_Send_DATA(m_55);		//µ±Êı¾İÇø³öÏÖFFÊ±£¬×·¼Ó·¢ËÍ55£¬°üÍ·µÄFF FF²»ÄÜ×·¼Ó55
	}
	
	//no need to wait ack
	if(tag == 0) return ;
	
	memcpy(&send_headPart, buf, sizeof(pro_headPart));
	memset(&recv_commonCmd, 0, sizeof(pro_commonCmd));
	
	wait_ack_time = 0;
	Send_num = 1;
	
	while(Send_num < MAX_SEND_NUM)
	{
		if(wait_ack_time < MAX_SEND_TIME)
		{
			if(get_one_package)
			{				
				get_one_package = 0;
				memcpy(&recv_commonCmd, uart_buf, sizeof(pro_commonCmd));
				
				//Ö»ÓĞµ±snºÍACK¾ùÅä¶ÔÊ±£¬²Å±íÃ÷·¢ËÍ³É¹¦£»
				if((send_headPart.cmd == CMD_SEND_MODULE_P0 && recv_commonCmd.head_part.cmd == CMD_SEND_MODULE_P0_ACK) &&
					(send_headPart.sn == recv_commonCmd.head_part.sn)) break;
				
				if((send_headPart.cmd == CMD_SET_MODULE_WORKMODE && recv_commonCmd.head_part.cmd == CMD_SET_MODULE_WORKMODE_ACK) &&
					(send_headPart.sn == recv_commonCmd.head_part.sn)) break;
				
				if((send_headPart.cmd == CMD_RESET_MODULE && recv_commonCmd.head_part.cmd == CMD_RESET_MODULE_ACK) &&
					(send_headPart.sn == recv_commonCmd.head_part.sn)) break;
			}
		}
		else 
		{
				wait_ack_time = 0 ;
			  for(i=0;i<packLen;i++){
					UART1_Send_DATA(buf[i]);
					if(i >=2 && buf[i] == 0xFF) UART1_Send_DATA(m_55);
				}
				Send_num ++;		
		}	
	}	
}

/*******************************************************************************
* Function Name  : CheckSum
* Description    : Ğ£ÑéºÍËã·¨
* Input          : buf:Êı¾İÆğÊ¼µØÖ·£» packLen:Êı¾İ³¤¶È£»
* Output         : None
* Return         : Ğ£ÑéÂë
* Attention		   : None
*******************************************************************************/
uint8_t CheckSum( uint8_t *buf, int packLen )
{
  int				i;
	uint8_t		sum;
	
	if(buf == NULL || packLen <= 0) return 0;

	sum = 0;
	for(i=2; i<packLen-1; i++) sum += buf[i];

	return sum;
}


/*******************************************************************************
* Function Name  : ReportStatus
* Description    : ÉÏ±¨mcu×´Ì¬
* Input          : tag=REPORT_STATUS£¬Ö÷¶¯ÉÏ±¨£¬Ê¹ÓÃCMD_SEND_MODULE_P0ÃüÁî×Ö£»
									 tag=REQUEST_STATUS£¬±»¶¯²éÑ¯£¬Ê¹ÓÃCMD_SEND_MCU_P0_ACKÃüÁî×Ö£»
* Output         : None
* Return         : None
* Attention		   : None
*******************************************************************************/
void ReportStatus(uint8_t tag)
{
  //Ö÷¶¯ÉÏ±¨
	if(tag == REPORT_STATUS)
	{
		m_m2w_mcuStatus.head_part.cmd = CMD_SEND_MODULE_P0;
		m_m2w_mcuStatus.head_part.sn = ++SN;
		m_m2w_mcuStatus.sub_cmd = SUB_CMD_REPORT_MCU_STATUS;
		//m_m2w_mcuStatus.status_w.motor_speed = exchangeBytes(m_m2w_mcuStatus.status_w.motor_speed);
		m_m2w_mcuStatus.sum = CheckSum((uint8_t *)&m_m2w_mcuStatus, sizeof(m2w_mcuStatus));
		SendToUart((uint8_t *)&m_m2w_mcuStatus, sizeof(m2w_mcuStatus), 1);
	}
  //±»¶¯ÉÏ±¨
	else if(tag == REQUEST_STATUS)
	{
		m_m2w_mcuStatus.head_part.cmd = CMD_SEND_MCU_P0_ACK;
		m_m2w_mcuStatus.head_part.sn = m_w2m_controlMcu.head_part.sn;
		m_m2w_mcuStatus.sub_cmd = SUB_CMD_REQUIRE_STATUS_ACK;
		//m_m2w_mcuStatus.status_w.motor_speed = exchangeBytes(m_m2w_mcuStatus.status_w.motor_speed);
		m_m2w_mcuStatus.sum = CheckSum((uint8_t *)&m_m2w_mcuStatus, sizeof(m2w_mcuStatus));
		SendToUart((uint8_t *)&m_m2w_mcuStatus, sizeof(m2w_mcuStatus), 0);
	}
		

	//m_m2w_mcuStatus.status_w.motor_speed = exchangeBytes(m_m2w_mcuStatus.status_w.motor_speed);
	memcpy(&m_m2w_mcuStatus_reported, &m_m2w_mcuStatus, sizeof(m2w_mcuStatus));
}


/*******************************************************************************
* Function Name  : CheckStatus
* Description    : ¼ì²émcu×´Ì¬£¬ÊÇ·ñĞèÒªÖ÷¶¯ÉÏ±¨
* Input          : None
* Output         : None
* Return         : None
* Attention		   : 	1¡¢×î¿ì2Ãë¼ì²éÒ»´Î×´Ì¬ÊÇ·ñÓĞ±ä»¯£»
										2¡¢¼ì²é×îĞÂµÄmcuµÄ×´Ì¬Óë·¢ËÍ¹ıµÄ×´Ì¬ÊÇ·ñÏàÍ¬£¬²»Í¬¾Í·¢ËÍ£»
										3¡¢ÈôÊ±¼ä¼ä¸ô´óÓÚ10·ÖÖÓ£¬ÎŞÂÛ×´Ì¬ÊÇ·ñ±ä»¯£¬¶¼ÒªÉÏ±¨Ò»´Î×´Ì¬£»
*******************************************************************************/
void	CheckStatus(void)
{
	int					i, diff;
	uint8_t			*index_new, *index_old;
	
	diff = 0;
	DHT11_Read_Data(&m_m2w_mcuStatus.status_r.temputure, &m_m2w_mcuStatus.status_r.humidity);
	
  return ;
	if(check_status_time < 200) return ;
		
	check_status_time = 0;
	index_new = (uint8_t *)&(m_m2w_mcuStatus.status_w);
	index_old = (uint8_t *)&(m_m2w_mcuStatus_reported.status_w);
		
	for(i=0; i<sizeof(status_writable); i++)
	{
		if(*(index_new+i) != *(index_old+i)) diff += 1;
	}
		
	if(diff == 0)
	{
		index_new = (uint8_t *)&(m_m2w_mcuStatus.status_r);
		index_old = (uint8_t *)&(m_m2w_mcuStatus_reported.status_r);
			
		for(i=0; i<sizeof(status_readonly); i++)
		{
			if(*(index_new+i) != *(index_old+i)) diff += 1;
		}
	}
	
	if(diff > 0 || report_status_idle_time > 60000) ReportStatus(REPORT_STATUS);
	if(report_status_idle_time > 60000) report_status_idle_time = 0;
	
}
